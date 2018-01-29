class SpeedControl implements Runnable {
    final static int DISABLED = 0; //speed control states   ����
    final static int ENABLED  = 1; //����
    volatile private int state = DISABLED;  //initial state ��ʼ״̬
    volatile private int setSpeed = 0;      //target cruise control speed Ŀ��Ѳ�������ٶȣ�
    volatile private Thread speedController;
    volatile private CarSpeed cs;         //interface to control speed of engine ���������Ʒ�������ת��
    volatile private CruiseDisplay disp;

    SpeedControl(CarSpeed cs, CruiseDisplay disp){
        this.cs=cs; this.disp=disp;
        disp.disabled(); disp.record(0);    //���� �ٶ�0
    }

    //��ȡ��ǰ�ٶ�
    synchronized void recordSpeed(){
        setSpeed=cs.getSpeed(); disp.record(setSpeed);
    }

    //�ٶ���0
    synchronized void clearSpeed(){
        if (state==DISABLED) {setSpeed=0;disp.record(setSpeed);}
    }

    //���ÿ���
    synchronized void enableControl(){
        if (state==DISABLED) {
            disp.enabled();
            //�����µ��̣߳�����״̬
            speedController= new Thread(this);
            speedController.start();
            state=ENABLED;
        }
    }

    //���ÿ���
    synchronized void disableControl(){
        if (state==ENABLED)  {disp.disabled(); state=DISABLED;}
    }

    synchronized public void run() {     // the speed controller thread �ٶȿ������߳�
        try {
            while (state==ENABLED) {
                double error = (float)(setSpeed-cs.getSpeed())/6.0;
                //�ȶ�
                double steady = (double)setSpeed/12.0;
                cs.setThrottle(steady+error); //simplified feed back control �򻯷�������
                //�õ�ǰ�߳̽���ȴ�״̬ �õ�ǰ�߳��ͷ��������е��� 500ms
                wait(500);
            }
        }
        catch (InterruptedException e) {}
        speedController=null;
    }
}

