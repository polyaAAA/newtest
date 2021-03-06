package com.penglab.hi5.chat.nim.main.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.penglab.hi5.chat.nim.main.adapter.SystemMessageAdapter;
import com.penglab.hi5.chat.nim.main.viewholder.SystemMessageViewHolder;
import com.penglab.hi5.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.listview.AutoRefreshListView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nimlib.sdk.InvocationFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.constant.SystemMessageStatus;
import com.netease.nimlib.sdk.msg.constant.SystemMessageType;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.superteam.SuperTeamService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ????????????????????????
 * <p/>
 * Created by huangjun on 2015/3/18.
 */
public class SystemMessageActivity extends UI implements TAdapterDelegate,
        AutoRefreshListView.OnRefreshListener, SystemMessageViewHolder.SystemMessageListener {

    private static final boolean MERGE_ADD_FRIEND_VERIFY = false; // ???????????????????????????????????????????????????????????????????????????????????????????????????

    private static final int LOAD_MESSAGE_COUNT = 10;
    private static final String TAG = "SystemMessageActivity";

    // view
    private MessageListView listView;

    // adapter
    private SystemMessageAdapter adapter;
    private List<SystemMessage> items = new ArrayList<>();
    private Set<Long> itemIds = new HashSet<>();

    // db
    private boolean firstLoad = true;

    public static void start(Context context) {
        start(context, null, true);
    }

    public static void start(Context context, Intent extras, boolean clearTop) {
        Intent intent = new Intent();
        intent.setClass(context, SystemMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return SystemMessageViewHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }


    @Override
    public void onRefreshFromStart() {
    }

    @Override
    public void onRefreshFromEnd() {
        loadMessages(); // load old data
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.system_notification_message_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.verify_reminder;
        setToolBar(R.id.toolbar, options);

        initAdapter();
        initListView();

        loadMessages(); // load old data
        registerSystemObserver(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onStop() {
        super.onStop();

        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerSystemObserver(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification_menu_btn:
                deleteAllMessages();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAdapter() {
        adapter = new SystemMessageAdapter(this, items, this, this);
    }

    private void initListView() {
        listView = (MessageListView) findViewById(R.id.messageListView);
        listView.setMode(AutoRefreshListView.Mode.END);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        // adapter
        listView.setAdapter(adapter);

        // listener
        listView.setOnRefreshListener(this);
    }


    private int loadOffset = 0;
    private Set<String> addFriendVerifyRequestAccounts = new HashSet<>(); // ?????????????????????????????????????????????????????????

    /**
     * ??????????????????
     */
    public void loadMessages() {
        listView.onRefreshStart(AutoRefreshListView.Mode.END);
        boolean loadCompleted; // ????????????????????????????????????????????????or??????????????????????????????
        int validMessageCount = 0; // ????????????????????????????????????????????????????????????
        List<String> messageFromAccounts = new ArrayList<>(LOAD_MESSAGE_COUNT);

        while (true) {
            List<SystemMessage> temps = NIMClient.getService(SystemMessageService.class)
                    .querySystemMessagesBlock(loadOffset, LOAD_MESSAGE_COUNT);

            loadOffset += temps.size();
            loadCompleted = temps.size() < LOAD_MESSAGE_COUNT;

            int tempValidCount = 0;

            for (SystemMessage m : temps) {
                // ??????
                if (duplicateFilter(m)) {
                    continue;
                }

                // ???????????????????????????????????????????????????
                if (addFriendVerifyFilter(m)) {
                    continue;
                }

                // ??????????????????
                items.add(m);
                tempValidCount++;
                if (!messageFromAccounts.contains(m.getFromAccount())) {
                    messageFromAccounts.add(m.getFromAccount());
                }

                // ???????????????????????????
                if (++validMessageCount >= LOAD_MESSAGE_COUNT) {
                    loadCompleted = true;
                    // ???????????????????????????????????????loadOffset
                    loadOffset -= temps.size();
                    loadOffset += tempValidCount;

                    break;
                }
            }

            if (loadCompleted) {
                break;
            }
        }

        // ??????????????????????????????
        refresh();

        boolean first = firstLoad;
        firstLoad = false;
        if (first) {
            ListViewUtil.scrollToPosition(listView, 0, 0); // ??????????????????????????????
        }

        listView.onRefreshComplete(validMessageCount, LOAD_MESSAGE_COUNT, true);

        // ?????????????????????????????????????????????????????????
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }

    /**
     * ???????????????
     */
    private void onIncomingMessage(final SystemMessage message) {
        // ???????????????????????????????????????????????????
        if (addFriendVerifyFilter(message)) {
            SystemMessage del = null;
            for (SystemMessage m : items) {
                if (m.getFromAccount().equals(message.getFromAccount()) && m.getType() == SystemMessageType.AddFriend) {
                    AddFriendNotify attachData = (AddFriendNotify) m.getAttachObject();
                    if (attachData != null && attachData.getEvent() == AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
                        del = m;
                        break;
                    }
                }
            }

            if (del != null) {
                items.remove(del);
            }
        }

        loadOffset++;
        items.add(0, message);

        // ????????????????????????????????????????????????
        String customInfo = message.getCustomInfo();
        Log.e(TAG, "system message , customInfo = " + customInfo);

        // ??????????????????????????????????????????????????????????????????
        String content = message.getContent();
        Log.e(TAG, "system message , content = " + content);

        refresh();

        // ?????????????????????????????????????????????????????????
        List<String> messageFromAccounts = new ArrayList<>(1);
        messageFromAccounts.add(message.getFromAccount());
        collectAndRequestUnknownUserInfo(messageFromAccounts);
    }


    // ??????
    private boolean duplicateFilter(final SystemMessage msg) {
        if (itemIds.contains(msg.getMessageId())) {
            return true;
        }

        itemIds.add(msg.getMessageId());
        return false;
    }

    // ???????????????????????????????????????????????????
    private boolean addFriendVerifyFilter(final SystemMessage msg) {
        if (!MERGE_ADD_FRIEND_VERIFY) {
            return false; // ?????????MERGE????????????
        }

        if (msg.getType() != SystemMessageType.AddFriend) {
            return false; // ?????????
        }

        AddFriendNotify attachData = (AddFriendNotify) msg.getAttachObject();
        if (attachData == null) {
            return true; // ??????
        }

        if (attachData.getEvent() != AddFriendNotify.Event.RECV_ADD_FRIEND_VERIFY_REQUEST) {
            return false; // ?????????
        }

        if (addFriendVerifyRequestAccounts.contains(msg.getFromAccount())) {
            return true; // ??????
        }

        addFriendVerifyRequestAccounts.add(msg.getFromAccount());
        return false; // ?????????
    }

    // ???????????????????????????
    private void collectAndRequestUnknownUserInfo(List<String> messageFromAccounts) {
        List<String> unknownAccounts = new ArrayList<>();
        for (String account : messageFromAccounts) {
            if (NimUIKit.getUserInfoProvider().getUserInfo(account) == null) {
                unknownAccounts.add(account);
            }
        }

        if (!unknownAccounts.isEmpty()) {
            requestUnknownUser(unknownAccounts);
        }
    }

    @Override
    public void onAgree(SystemMessage message) {
        onSystemNotificationDeal(message, true);
    }

    @Override
    public void onReject(SystemMessage message) {
        onSystemNotificationDeal(message, false);
    }

    @Override
    public void onLongPressed(SystemMessage message) {
        showLongClickMenu(message);
    }

    private void onSystemNotificationDeal(final SystemMessage message, final boolean pass) {
        RequestCallback callback = new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                onProcessSuccess(pass, message);
            }

            @Override
            public void onFailed(int code) {
                onProcessFailed(code, message);
            }

            @Override
            public void onException(Throwable exception) {

            }
        };

        TeamService teamService = NIMClient.getService(TeamService.class);
        SuperTeamService superTeamService = NIMClient.getService(SuperTeamService.class);
        InvocationFuture<Void> invocationFuture = null;
        String targetId = message.getTargetId();
        String fromAccount = message.getFromAccount();
        switch (message.getType()) {
            case TeamInvite:
                invocationFuture = pass ? teamService.acceptInvite(targetId, fromAccount) : teamService.declineInvite(targetId, fromAccount, "");
                break;
            case ApplyJoinTeam:
                invocationFuture = pass ? teamService.passApply(targetId, fromAccount) : teamService.rejectApply(targetId, fromAccount, "");
                break;
            case SuperTeamInvite:
                invocationFuture = pass ? superTeamService.acceptInvite(targetId, fromAccount) : superTeamService.declineInvite(targetId, fromAccount, "");
                break;
            case SuperTeamApply:
                invocationFuture = pass ? superTeamService.passApply(targetId, fromAccount) : superTeamService.rejectApply(targetId, fromAccount, "");
                break;
            case AddFriend:
                invocationFuture = NIMClient.getService(FriendService.class).ackAddFriendRequest(fromAccount, pass);
                break;
            default:
                break;
        }
        if (invocationFuture != null) {
            invocationFuture.setCallback(callback);
        }
    }

    private void onProcessSuccess(final boolean pass, SystemMessage message) {
        SystemMessageStatus status = pass ? SystemMessageStatus.passed : SystemMessageStatus.declined;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void onProcessFailed(final int code, SystemMessage message) {
        ToastHelper.showToastLong(SystemMessageActivity.this, "failed, error code=" + code);
        if (code == 408) {
            return;
        }

        SystemMessageStatus status = SystemMessageStatus.expired;
        NIMClient.getService(SystemMessageService.class).setSystemMessageStatus(message.getMessageId(),
                status);
        message.setStatus(status);
        refreshViewHolder(message);
    }

    private void refresh() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void refreshViewHolder(final SystemMessage message) {
        final long messageId = message.getMessageId();

        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            SystemMessage item = items.get(i);
            if (messageId == item.getMessageId()) {
                index = i;
                break;
            }
        }

        if (index < 0) {
            return;
        }

        final int m = index;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m < 0) {
                    return;
                }

                Object tag = ListViewUtil.getViewHolderByIndex(listView, m);
                if (tag instanceof SystemMessageViewHolder) {
                    ((SystemMessageViewHolder) tag).refreshDirectly(message);
                }
            }
        });
    }

    private void registerSystemObserver(boolean register) {
        NIMClient.getService(SystemMessageObserver.class).observeReceiveSystemMsg(systemMessageObserver, register);
    }

    Observer<SystemMessage> systemMessageObserver = new Observer<SystemMessage>() {
        @Override
        public void onEvent(SystemMessage systemMessage) {
            onIncomingMessage(systemMessage);
        }
    };

    private void requestUnknownUser(List<String> accounts) {
        NimUIKit.getUserInfoProvider().getUserInfoAsync(accounts, new SimpleCallback<List<NimUserInfo>>() {
            @Override
            public void onResult(boolean success, List<NimUserInfo> result, int code) {
                if (code == ResponseCode.RES_SUCCESS) {
                    if (result != null && !result.isEmpty()) {
                        refresh();
                    }
                }
            }
        });
    }

    private void deleteAllMessages() {
        NIMClient.getService(SystemMessageService.class).clearSystemMessages();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
        items.clear();
        refresh();
        ToastHelper.showToast(SystemMessageActivity.this, R.string.clear_all_success);
    }

    private void showLongClickMenu(final SystemMessage message) {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.setTitle(R.string.delete_tip);
        String title = getString(R.string.delete_system_message);
        alertDialog.addItem(title, new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                deleteSystemMessage(message);
            }
        });
        alertDialog.show();
    }

    private void deleteSystemMessage(final SystemMessage message) {
        NIMClient.getService(SystemMessageService.class).deleteSystemMessage(message.getMessageId());
        items.remove(message);
        refresh();
        ToastHelper.showToast(SystemMessageActivity.this, R.string.delete_success);
    }
}

