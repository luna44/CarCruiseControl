class SpeedControl implements Runnable {
    final static int DISABLED = 0; //speed control states   禁用
    final static int ENABLED  = 1; //启用
    volatile private int state = DISABLED;  //initial state 初始状态
    volatile private int setSpeed = 0;      //target cruise control speed 目标巡航控制速度？
    volatile private Thread speedController;
    volatile private CarSpeed cs;         //interface to control speed of engine 界面来控制发动机的转速
    volatile private CruiseDisplay disp;

    SpeedControl(CarSpeed cs, CruiseDisplay disp){
        this.cs=cs; this.disp=disp;
        disp.disabled(); disp.record(0);    //禁用 速度0
    }

    //获取当前速度
    synchronized void recordSpeed(){
        setSpeed=cs.getSpeed(); disp.record(setSpeed);
    }

    //速度置0
    synchronized void clearSpeed(){
        if (state==DISABLED) {setSpeed=0;disp.record(setSpeed);}
    }

    //启用控制
    synchronized void enableControl(){
        if (state==DISABLED) {
            disp.enabled();
            //创建新的线程，启动状态
            speedController= new Thread(this);
            speedController.start();
            state=ENABLED;
        }
    }

    //禁用控制
    synchronized void disableControl(){
        if (state==ENABLED)  {disp.disabled(); state=DISABLED;}
    }

    synchronized public void run() {     // the speed controller thread 速度控制器线程
        try {
            while (state==ENABLED) {
                double error = (float)(setSpeed-cs.getSpeed())/6.0;
                //稳定
                double steady = (double)setSpeed/12.0;
                cs.setThrottle(steady+error); //simplified feed back control 简化反馈控制
                //让当前线程进入等待状态 让当前线程释放它所持有的锁 500ms
                wait(500);
            }
        }
        catch (InterruptedException e) {}
        speedController=null;
    }
}

