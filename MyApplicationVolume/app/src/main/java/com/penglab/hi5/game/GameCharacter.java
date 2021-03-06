package com.penglab.hi5.game;

import android.opengl.Matrix;

import java.util.ArrayList;

public class GameCharacter {
    private float [] position;
    private float [] dir;
    private float [] head;

    private float [] thirdPosition;
    private float [] thirdDir;
    private float [] thirdHead;

    public GameCharacter(){
        position = new float[3];
        dir = new float[3];
        head = new float[3];
        thirdPosition = new float[3];
        thirdDir = new float[3];
        thirdHead = new float[3];
    }

    public GameCharacter(float [] position, float [] dir, float [] head){
        this.position = position;
        this.dir = dir;
        this.head = head;
        thirdPosition = new float[3];
        thirdDir = new float[3];
        thirdHead = new float[3];
    }

    public void setPosition(float [] position){
        this.position = position;
    }

    public void setDir(float [] dir){
        this.dir = dir;
    }

    public void setHead(float [] head){
        this.head = head;
    }

    public float [] getPosition(){
        return position;
    }

    public float [] getDir(){
        return dir;
    }

    public float [] getHead(){
        return head;
    }

    public void setThirdPosition(float [] thirdPos){
        this.thirdPosition = thirdPos;
    }

    public void setThirdDir(float [] thirdDir){
        this.thirdDir = thirdDir;
    }

    public void setThirdHead(float [] thirdHead){
        this.thirdHead = thirdHead;
    }

    public float [] getThirdPosition(){
        return thirdPosition;
    }

    public float [] getThirdDir(){
        return thirdDir;
    }

    public float [] getThirdHead() {
        return thirdHead;
    }

    public void rotateDir(float angleH, float angleV){
        float [] dirE = new float[]{dir[0], dir[1], dir[2], 1};
        float [] headE = new float[]{head[0], head[1], head[2], 1};

        if (angleH != 0 && angleV != 0) {

            float[] rotationHMatrix = new float[16];
            float[] rotationVMatrix = new float[16];

            Matrix.setRotateM(rotationHMatrix, 0, angleH, head[0], head[1], head[2]);

            Matrix.multiplyMV(dirE, 0, rotationHMatrix, 0, dirE, 0);

            float [] axisV = new float[]{dirE[1] * head[2] - dirE[2] * head[1], dirE[2] * head[0] - dirE[0] * head[2], dirE[0] * head[1] - dirE[1] * head[0]};

            Matrix.setRotateM(rotationVMatrix, 0, -angleV, axisV[0], axisV[1], axisV[2]);

            Matrix.multiplyMV(dirE, 0, rotationVMatrix, 0, dirE, 0);
            Matrix.multiplyMV(headE, 0, rotationVMatrix, 0, headE, 0);

            dir = new float[]{dirE[0], dirE[1], dirE[2]};
            head = new float[]{headE[0], headE[1], headE[2]};
        }
    }

    public void movePosition(float x, float y){


        float [] axisV = new float[]{dir[1] * head[2] - dir[2] * head[1], dir[2] * head[0] - dir[0] * head[2], dir[0] * head[1] - dir[1] * head[0]};
        float XL = (float)Math.sqrt(axisV[0] * axisV[0] + axisV[1] * axisV[1] + axisV[2] * axisV[2]);
        float [] X = new float[]{axisV[0] / XL, axisV[1] / XL, axisV[2] / XL};
        float YL = (float)Math.sqrt(dir[0] * dir[0] + dir[1] * dir[1] + dir[2] * dir[2]);
        float [] Y = new float[]{dir[0] / YL, dir[1] / YL, dir[2] / YL};

        position[0] = position[0] + X[0] * x - Y[0] * y;
        position[1] = position[1] + X[1] * x - Y[1] * y;
        position[2] = position[2] + X[2] * x - Y[2] * y;

//            myrenderer.clearMarkerList();
//            myrenderer.addMarker(position);

    }

    public void thirdPersonAngle(float behind, float up, float front, float [] thirdPos, float [] thirdDir){
        // (x0,y0,z0)???mark???????????????m,n,p??????mark?????????????????????,(h1,h2,h3)??????????????????????????????
        //???behind????????????????????????mark??????????????????
        //???up????????????????????????mark?????????????????????
        //???front?????????mark???????????????????????????????????????????????????????????????,??????????????????????????????
        // 1.??????????????????????????????????????? ??????????????????????????????????????????XOZ ????????????????????????XOZ??????????????????0,1,0??????,??????????????????????????????????????????
        // ?????????????????????????????????????????????????????????(????????????????????????????????????)?????????????????????????????????????????????????????????????????????
        // ????????????(a1,b1,c1)???(a2,b2,c2) ????????? (b1c2-b2c1,a2c1-a1c2,a1b2-a2b1)
        // ????????????(m,n,p)??? (a1,b1,c1)
        // 2.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????p(m^2-n^2-p^2)(X-x0)+m(p^2-m^2-n^2)(Z-z0)=0,??????Y????????????????????????Y???
        // ??????????????????????????????. ?????????mark??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        float[] XOZ = {0,1,0}; // (a2,b2,c2)
//        float[] dir_ver = new float[3];

        float [] tempDir = new float[]{dir[0], dir[1], dir[2]};
        float x0 = position[0];
        float y0 = position[1];
        float z0 = position[2];
        float [] tempHead = new float[]{head[0], head[1], head[2]};
        float[] des = new float[6]; //???????????????????????????????????????????????????????????????????????????

//        final float rad = (float) (45*(Math.PI/180)); //??????????????????????????????????????? ?????????45??????????????????????????????????????????????????????

//        dir_ver[0] = p; //a2
//        dir_ver[1] = 0; //b2
//        dir_ver[2] = m; //c2
        float tempDirLength = (float)Math.sqrt(tempDir[0] * tempDir[0] + tempDir[1] * tempDir[1] + tempDir[2] * tempDir[2]);
        tempDir[0] = tempDir[0] / tempDirLength;
        tempDir[1] = tempDir[1] / tempDirLength;
        tempDir[2] = tempDir[2] / tempDirLength; //???????????????????????????????????????????????????1 ?????????????????????????????????????????????block?????????1???

        tempHead[0] = (float) (head[0]/Math.sqrt(head[0]*head[0]+head[1]*head[1]+head[2]*head[2]));
        tempHead[1] = (float) (head[1]/Math.sqrt(head[0]*head[0]+head[1]*head[1]+head[2]*head[2]));
        tempHead[2] = (float) (head[2]/Math.sqrt(head[0]*head[0]+head[1]*head[1]+head[2]*head[2]));


        des[0] = x0-behind*tempDir[0]+up*tempHead[0];
        des[1] = y0-behind*tempDir[0]+up*tempHead[1];
        des[2] = z0-behind*tempDir[0]+up*tempHead[2];  // ?????????????????????????????????????????????mark????????????????????????????????????????????????????????????

        float[] aid = {des[0]-x0,des[1]-y0,des[2]-z0}; // ?????????????????????????????????
        if ((tempHead[0]*aid[0]+tempHead[1]*aid[1]+tempHead[2]*aid[2])/(Math.sqrt(tempHead[0]*tempHead[0]+tempHead[1]*tempHead[1]+tempHead[2]*tempHead[2])*Math.sqrt(aid[0]*aid[0]+aid[1]*aid[1]+aid[2]*aid[2])) < 0){
            des[0] = x0-behind*tempDir[0]-up*tempHead[0];
            des[1] = y0-behind*tempDir[1]-up*tempHead[1];
            des[2] = z0-behind*tempDir[2]-up*tempHead[2];  // ????????????????????????????????????????????? ??????????????? ??????????????????????????????
        }

        des[3] = (x0+front*tempDir[0])-des[0];
        des[4] = (y0+front*tempDir[1])-des[1];
        des[5] = (z0+front*tempDir[2])-des[2];

//        thirdPos = new float[]{des[0], des[1], des[2]};
        thirdPos[0] = des[0];
        thirdPos[1] = des[1];
        thirdPos[2] = des[2];
//        thirdDir = new float[]{des[3], des[4], des[5]};
        thirdDir[0] = des[3];
        thirdDir[1] = des[4];
        thirdDir[2] = des[5];

    }

    public void setThirdPersonal(){

        thirdPersonAngle(0.1f, 0.03f, 0.3f, thirdPosition, thirdDir);

        float [] axis = new float[]{thirdDir[1] * head[2] - head[1] * thirdDir[2], thirdDir[2] * head[0] - head[2] * thirdDir[0], thirdDir[0] * head[1] - head[0] * thirdDir[1]};

//        float [] thirdHead = locateHead(dir[0], dir[1], dir[2]);
        thirdHead = new float[]{axis[1] * thirdDir[2] - thirdDir[1] * axis[2], axis[2] * thirdDir[0] - axis[0] * thirdDir[2], axis[0] * thirdDir[1] - axis[1] * thirdDir[0]};
        float acos = thirdHead[0] * head[0] + thirdHead[1] * head[1] + thirdHead[2] * head[2];
        if (acos > 0){

        } else {
            thirdHead = new float[]{-thirdHead[0], -thirdHead[1], -thirdHead[2]};
        }
    }

    public ArrayList<Float> tangentPlane(float [] pos, float [] d, float Pix){
// (x0,y0,z0)????????????????????????(m,n,p)???????????????t??????????????????????????????t???Pix???block?????????
        ArrayList<Float> sec = new ArrayList<Float>();
        float x1 = pos[0]; // + m*t;
        float y1 = pos[1]; // + n*t;
        float z1 = pos[2]; // + p*t;

        float m = d[0];
        float n = d[1];
        float p = d[2];

        if (m!=0  & ((n*y1+p*z1)/m+x1) <= Pix & ((n*y1+p*z1)/m+x1)>=0){
            sec.add((n*y1+p*z1)/m+x1);
            sec.add((float) 0.0);
            sec.add((float) 0.0);
        }
        if (m!=0 & ((n*y1+p*(z1-Pix))/m+x1)<=Pix & ((n*y1+p*(z1-Pix))/m+x1)>=0){
            sec.add((n*y1+p*(z1-Pix))/m+x1);
            sec.add((float)0.0);
            sec.add(Pix);
        }
        if (m!=0 & ((n*(y1-Pix)+p*(z1-Pix))/m+x1)<=Pix & ((n*(y1-Pix)+p*(z1-Pix))/m+x1)>=0){
            sec.add((n*(y1-Pix)+p*(z1-Pix))/m+x1);
            sec.add(Pix);
            sec.add(Pix);
        }
        if (m!=0 & ((n*(y1-Pix)+p*z1)/m+x1)<=Pix & ((n*(y1-Pix)+p*z1)/m+x1)>=0){
            sec.add((n*(y1-Pix)+p*z1)/m+x1);
            sec.add(Pix);
            sec.add((float)0.0);
        }

        if (n!=0 & ((m*x1+p*z1)/n+y1)<Pix & ((m*x1+p*z1)/n+y1)>0){
            sec.add((float)0.0);
            sec.add((m*x1+p*z1)/n+y1);
            sec.add((float)0.0);
        }
        if (n!=0 & ((m*x1+p*(z1-Pix))/n+y1)<Pix & ((m*x1+p*(z1-Pix))/n+y1)>0){
            sec.add((float)0.0);
            sec.add((m*x1+p*(z1-Pix))/n+y1);
            sec.add(Pix);
        }
        if (n!=0 & ((m*(x1-Pix)+p*z1)/n+y1)<Pix & ((m*(x1-Pix)+p*z1)/n+y1)>0){
            sec.add(Pix);
            sec.add((m*(x1-Pix)+p*z1)/n+y1);
            sec.add((float)0.0);
        }
        if(n!=0 & ((m*(x1-Pix)+p*(z1-Pix))/n+y1)<Pix & ((m*(x1-Pix)+p*(z1-Pix))/n+y1)>0){
            sec.add(Pix);
            sec.add((m*(x1-Pix)+p*(z1-Pix))/n+y1);
            sec.add(Pix);
        }

        if (p!=0 & ((m*x1+n*y1)/p+z1)<Pix & ((m*x1+n*y1)/p+z1)>0){
            sec.add((float)0.0);
            sec.add((float)0.0);
            sec.add((m*x1+n*y1)/p+z1);
        }
        if (p!=0 & ((m*x1+n*(y1-Pix))/p+z1)<Pix & ((m*x1+n*(y1-Pix))/p+z1)>0){
            sec.add((float)0.0);
            sec.add(Pix);
            sec.add((m*x1+n*(y1-Pix))/p+z1);
        }
        if (p!=0 & ((m*(x1-Pix)+n*y1)/p+z1)<Pix & ((m*(x1-Pix)+n*y1)/p+z1)>0){
            sec.add(Pix);
            sec.add((float)0.0);
            sec.add((m*(x1-Pix)+n*y1)/p+z1);
        }
        if (p!=0 & ((m*(x1-Pix)+n*(y1-Pix))/p+z1)<Pix & ((m*(x1-Pix)+n*(y1-Pix))/p+z1)>0){
            sec.add(Pix);
            sec.add(Pix);
            sec.add((m*(x1-Pix)+n*(y1-Pix))/p+z1);
        }

        return sec;
    }

    public ArrayList<Float> sortVertex(ArrayList<Float> tangent){
        ArrayList<Integer> sec_proj1 = new ArrayList<Integer>();
        ArrayList<Integer> sec_proj2 = new ArrayList<Integer>();
        ArrayList<Integer> sec_proj3 = new ArrayList<Integer>();
        ArrayList<Integer> sec_proj4 = new ArrayList<Integer>();
        ArrayList<Float> sec_anti = new ArrayList<Float>();
        ArrayList<Float> sec_copy = new ArrayList<Float>();
        float gravity_X = 0;
        float gravity_Y = 0;
        float gravity_Z = 0;

        sec_copy = (ArrayList<Float>) tangent.clone();

        System.out.println("TangentPlane:::::");
        System.out.println(tangent.size());

        for (int i=0;i<tangent.size();i+=3) {
            gravity_X += tangent.get(i);
        }
        for (int i=0;i<tangent.size();i+=3) {
            gravity_Y += tangent.get(i+1);
        }
        for (int i=0;i<tangent.size();i+=3) {
            gravity_Z += tangent.get(i+2);
        }
        gravity_X /= (tangent.size()/3);
        gravity_Y /= (tangent.size()/3);
        gravity_Z /= (tangent.size()/3);

        for (int i=0;i<tangent.size();i+=3) {
            tangent.set(i,tangent.get(i)-gravity_X);
        }
        for (int i=0;i<tangent.size();i+=3) {
            tangent.set(i+1, tangent.get(i+1)-gravity_Y);
        }
        for (int i=0;i<tangent.size();i+=3) {
            tangent.set(i+2, tangent.get(i+2)-gravity_Z);
        }

        //?????????????????????????????????
        if (thirdDir[2]==0)
        //???????????????????????????XOY????????????????????????????????????XOZ??????
        {
            for (int i=0;i<tangent.size();i+=3) {
                if(tangent.get(i)>=0 & tangent.get(i+2)>=0) {

                    sec_proj1.add(i);

                }// ????????????
                else if(tangent.get(i)<=0 & tangent.get(i+2)>=0) {

                    sec_proj2.add(i);

                }// ????????????
                else if(tangent.get(i)<=0 & tangent.get(i+2)<=0) {

                    sec_proj3.add(i);

                }// ????????????
                else if(tangent.get(i)>=0 & tangent.get(i+2)<=0) {

                    sec_proj4.add(i);

                }// ????????????

            }



            //??????????????????1???????????????????????????????????????????????????????????????????????????????????????????????????
            if (sec_proj1.size()>1) {
                for (int i=0;i<sec_proj1.size();i++) {
                    for (int j=0;j<sec_proj1.size()-i-1;j++) {
                        if(tangent.get(sec_proj1.get(j))!=0 & tangent.get(sec_proj1.get(j+1))!=0) {
                            if(tangent.get(sec_proj1.get(j)+2)/tangent.get(sec_proj1.get(j)) > tangent.get(sec_proj1.get(j+1)+2)/tangent.get(sec_proj1.get(j+1))) {
                                int temp = sec_proj1.get(j);
                                sec_proj1.set(j, sec_proj1.get(j+1));
                                sec_proj1.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj1.get(j))==0 & tangent.get(sec_proj1.get(j+1))==0) {
                                if(tangent.get(sec_proj1.get(j)+2)<tangent.get(sec_proj1.get(j+1)+2)) {
                                    int temp = sec_proj1.get(j);
                                    sec_proj1.set(j, sec_proj1.get(j+1));
                                    sec_proj1.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj1.get(j))==0) {
                                    int temp = sec_proj1.get(j);
                                    sec_proj1.set(j, sec_proj1.get(j+1));
                                    sec_proj1.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj2.size()>1) {
                for (int i=0;i<sec_proj2.size();i++) {
                    for (int j=0;j<sec_proj2.size()-i-1;j++) {
                        if(tangent.get(sec_proj2.get(j))!=0 & tangent.get(sec_proj2.get(j+1))!=0) {
                            if(tangent.get(sec_proj2.get(j)+2)/tangent.get(sec_proj2.get(j)) > tangent.get(sec_proj2.get(j+1)+2)/tangent.get(sec_proj2.get(j+1))) {
                                int temp = sec_proj2.get(j);
                                sec_proj2.set(j, sec_proj2.get(j+1));
                                sec_proj2.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj2.get(j))==0 & tangent.get(sec_proj2.get(j+1))==0) {
                                if(tangent.get(sec_proj2.get(j)+2)<tangent.get(sec_proj2.get(j+1)+2)) {
                                    int temp = sec_proj2.get(j);
                                    sec_proj2.set(j, sec_proj2.get(j+1));
                                    sec_proj2.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj2.get(j))==0) {
                                    int temp = sec_proj2.get(j);
                                    sec_proj2.set(j, sec_proj2.get(j+1));
                                    sec_proj2.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj3.size()>1) {
                for (int i=0;i<sec_proj3.size();i++) {
                    for (int j=0;j<sec_proj3.size()-i-1;j++) {
                        if(tangent.get(sec_proj3.get(j))!=0 & tangent.get(sec_proj3.get(j+1))!=0) {
                            if(tangent.get(sec_proj3.get(j)+2)/tangent.get(sec_proj3.get(j)) > tangent.get(sec_proj3.get(j+1)+2)/tangent.get(sec_proj3.get(j+1))) {
                                int temp = sec_proj3.get(j);
                                sec_proj3.set(j, sec_proj3.get(j+1));
                                sec_proj3.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj3.get(j))==0 & tangent.get(sec_proj3.get(j+1))==0) {
                                if(tangent.get(sec_proj3.get(j)+2)<tangent.get(sec_proj3.get(j+1)+2)) {
                                    int temp = sec_proj3.get(j);
                                    sec_proj3.set(j, sec_proj3.get(j+1));
                                    sec_proj3.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj3.get(j))==0) {
                                    int temp = sec_proj3.get(j);
                                    sec_proj3.set(j, sec_proj3.get(j+1));
                                    sec_proj3.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj4.size()>1) {
                for (int i=0;i<sec_proj4.size();i++) {
                    for (int j=0;j<sec_proj4.size()-i-1;j++) {
                        if(tangent.get(sec_proj4.get(j))!=0 & tangent.get(sec_proj4.get(j+1))!=0) {
                            if(tangent.get(sec_proj4.get(j)+2)/tangent.get(sec_proj4.get(j)) > tangent.get(sec_proj4.get(j+1)+2)/tangent.get(sec_proj4.get(j+1))) {
                                int temp = sec_proj4.get(j);
                                sec_proj4.set(j, sec_proj4.get(j+1));
                                sec_proj4.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj4.get(j))==0 & tangent.get(sec_proj4.get(j+1))==0) {
                                if(tangent.get(sec_proj4.get(j)+1)<tangent.get(sec_proj4.get(j+1)+1)) {
                                    int temp = sec_proj4.get(j);
                                    sec_proj4.set(j, sec_proj4.get(j+1));
                                    sec_proj4.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj4.get(j))==0) {
                                    int temp = sec_proj4.get(j);
                                    sec_proj4.set(j, sec_proj4.get(j+1));
                                    sec_proj4.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }


        }
        else {
            for (int i=0;i<tangent.size();i+=3) {
                if(tangent.get(i)>=0 & tangent.get(i+1)>=0) {

                    sec_proj1.add(i);

                }// ????????????
                else if(tangent.get(i)<=0 & tangent.get(i+1)>=0) {

                    sec_proj2.add(i);

                }// ????????????
                else if(tangent.get(i)<=0 & tangent.get(i+1)<=0) {

                    sec_proj3.add(i);

                }// ????????????
                else if(tangent.get(i)>=0 & tangent.get(i+1)<=0) {

                    sec_proj4.add(i);

                }// ????????????

            }




            //??????????????????1???????????????????????????????????????????????????????????????????????????????????????????????????
            if (sec_proj1.size()>1) {
                for (int i=0;i<sec_proj1.size();i++) {
                    for (int j=0;j<sec_proj1.size()-i-1;j++) {
                        if(tangent.get(sec_proj1.get(j))!=0 & tangent.get(sec_proj1.get(j+1))!=0) {
                            if(tangent.get(sec_proj1.get(j)+1)/tangent.get(sec_proj1.get(j)) > tangent.get(sec_proj1.get(j+1)+1)/tangent.get(sec_proj1.get(j+1))) {
                                int temp = sec_proj1.get(j);
                                sec_proj1.set(j, sec_proj1.get(j+1));
                                sec_proj1.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj1.get(j))==0 & tangent.get(sec_proj1.get(j+1))==0) {
                                if(tangent.get(sec_proj1.get(j)+1)<tangent.get(sec_proj1.get(j+1)+1)) {
                                    int temp = sec_proj1.get(j);
                                    sec_proj1.set(j, sec_proj1.get(j+1));
                                    sec_proj1.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj1.get(j))==0) {
                                    int temp = sec_proj1.get(j);
                                    sec_proj1.set(j, sec_proj1.get(j+1));
                                    sec_proj1.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj2.size()>1) {
                for (int i=0;i<sec_proj2.size();i++) {
                    for (int j=0;j<sec_proj2.size()-i-1;j++) {
                        if(tangent.get(sec_proj2.get(j))!=0 & tangent.get(sec_proj2.get(j+1))!=0) {
                            if(tangent.get(sec_proj2.get(j)+1)/tangent.get(sec_proj2.get(j)) > tangent.get(sec_proj2.get(j+1)+1)/tangent.get(sec_proj2.get(j+1))) {
                                int temp = sec_proj2.get(j);
                                sec_proj2.set(j, sec_proj2.get(j+1));
                                sec_proj2.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj2.get(j))==0 & tangent.get(sec_proj2.get(j+1))==0) {
                                if(tangent.get(sec_proj2.get(j)+1)<tangent.get(sec_proj2.get(j+1)+1)) {
                                    int temp = sec_proj2.get(j);
                                    sec_proj2.set(j, sec_proj2.get(j+1));
                                    sec_proj2.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj2.get(j))==0) {
                                    int temp = sec_proj2.get(j);
                                    sec_proj2.set(j, sec_proj2.get(j+1));
                                    sec_proj2.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj3.size()>1) {
                for (int i=0;i<sec_proj3.size();i++) {
                    for (int j=0;j<sec_proj3.size()-i-1;j++) {
                        if(tangent.get(sec_proj3.get(j))!=0 & tangent.get(sec_proj3.get(j+1))!=0) {
                            if(tangent.get(sec_proj3.get(j)+1)/tangent.get(sec_proj3.get(j)) > tangent.get(sec_proj3.get(j+1)+1)/tangent.get(sec_proj3.get(j+1))) {
                                int temp = sec_proj3.get(j);
                                sec_proj3.set(j, sec_proj3.get(j+1));
                                sec_proj3.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj3.get(j))==0 & tangent.get(sec_proj3.get(j+1))==0) {
                                if(tangent.get(sec_proj3.get(j)+1)<tangent.get(sec_proj3.get(j+1)+1)) {
                                    int temp = sec_proj3.get(j);
                                    sec_proj3.set(j, sec_proj3.get(j+1));
                                    sec_proj3.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj3.get(j))==0) {
                                    int temp = sec_proj3.get(j);
                                    sec_proj3.set(j, sec_proj3.get(j+1));
                                    sec_proj3.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }

            if (sec_proj4.size()>1) {
                for (int i=0;i<sec_proj4.size();i++) {
                    for (int j=0;j<sec_proj4.size()-i-1;j++) {
                        if(tangent.get(sec_proj4.get(j))!=0 & tangent.get(sec_proj4.get(j+1))!=0) {
                            if(tangent.get(sec_proj4.get(j)+1)/tangent.get(sec_proj4.get(j)) > tangent.get(sec_proj4.get(j+1)+1)/tangent.get(sec_proj4.get(j+1))) {
                                int temp = sec_proj4.get(j);
                                sec_proj4.set(j, sec_proj4.get(j+1));
                                sec_proj4.set(j+1, temp); //????????????
                            }
                        }
                        else {
                            if(tangent.get(sec_proj4.get(j))==0 & tangent.get(sec_proj4.get(j+1))==0) {
                                if(tangent.get(sec_proj4.get(j)+1)<tangent.get(sec_proj4.get(j+1)+1)) {
                                    int temp = sec_proj4.get(j);
                                    sec_proj4.set(j, sec_proj4.get(j+1));
                                    sec_proj4.set(j+1, temp); //????????????
                                }
                            }
                            else {
                                if(tangent.get(sec_proj4.get(j))==0) {
                                    int temp = sec_proj4.get(j);
                                    sec_proj4.set(j, sec_proj4.get(j+1));
                                    sec_proj4.set(j+1, temp); //????????????
                                }
                            }
                        }
                    }
                }
            }        }



        for(int i=0;i<sec_proj1.size();i++) {
            sec_anti.add(sec_copy.get(sec_proj1.get(i)));
            sec_anti.add(sec_copy.get(sec_proj1.get(i)+1));
            sec_anti.add(sec_copy.get(sec_proj1.get(i)+2));
        }
        for(int i=0;i<sec_proj2.size();i++) {
            sec_anti.add(sec_copy.get(sec_proj2.get(i)));
            sec_anti.add(sec_copy.get(sec_proj2.get(i)+1));
            sec_anti.add(sec_copy.get(sec_proj2.get(i)+2));
        }
        for(int i=0;i<sec_proj3.size();i++) {
            sec_anti.add(sec_copy.get(sec_proj3.get(i)));
            sec_anti.add(sec_copy.get(sec_proj3.get(i)+1));
            sec_anti.add(sec_copy.get(sec_proj3.get(i)+2));
        }
        for(int i=0;i<sec_proj4.size();i++) {
            sec_anti.add(sec_copy.get(sec_proj4.get(i)));
            sec_anti.add(sec_copy.get(sec_proj4.get(i)+1));
            sec_anti.add(sec_copy.get(sec_proj4.get(i)+2));
        }

        return sec_anti;
    }

    public boolean closeToBoundary(){
        if (position[0] > 0.8 || position[0] < 0.2 || position[1] > 0.8 || position[1] < 0.2 || position[2] > 0.8 || position[2] < 0.2)
            return true;

        return false;
    }

    public void move(float [] dir, float dis){

        position[0] += dir[0] * dis;
        position[1] += dir[1] * dis;
        position[2] += dir[2] * dis;
    }
}
