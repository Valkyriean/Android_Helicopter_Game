package li.juniorproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by Li on 2016/5/19.
 * 游戏主体
 * The body of game
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{

    //***************Integer Variables****************/
    //game width depends on background
    public static final int WIDTH = 960;
    //game height depends on background
    public static final int HEIGHT = 480;
    //move speed for both background and border
    public static final int MOVESPEED = -5;
    //maximum border height for both top and bottom, increasing while game going
    private int maxBorderHeight = 30;
    //minimum border height for both top and bottom, increasing while game going
    private int minBorderHeight = 5;
    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;
    //the best score history
    private int best;

    //******************Boolean Variables***********//
    //boolean shows if is resetting
    private boolean reset;
    //player should on the screen or not
    private boolean disappear;
    //after game reset, its waiting for user to start the game
    private boolean started;
    //top border is going down
    private boolean topDown = true;
    //bottom border is going down
//    private boolean botDown = true;
    //when everything is cleaned and a new game is ready to start
    private boolean newGameCreated;
    //is first game
    private boolean firstGame;
    private boolean better;

    //***********************Long Variables**************//
    //the time when smoke start creating, use to count down when need next smoke
    private long smokeStartTime;
    //refresh the screen
    private long missileStartTime;
    //the time when reset start
    private long startReset;
    //coin start time
    private long coinStartTime;

    //*******************Game Needed Objects******************//
    //I've no idea what thread is but it just work :p
    private MainThread thread;
    //background object
    private Background bg;
    //player object
    private Player player;
    //explosion animation object
    private Explosion explosion;
    //the BGM
    private MediaPlayer bgm;
    //the sound of explosion (infect using Atomic Bomb explosion sound effect)
    private MediaPlayer explosionSound;
    private MediaPlayer tada;
    //******************The Array List************************//
    //smokes
    private ArrayList<SmokePuff> smoke;
    //missiles array list
    private ArrayList<Missile> missiles;
    //top border
    private ArrayList<TopBorder> topBorders;
    //bottom border
    private ArrayList<BotBorder> botBorders;
    //coins
    private ArrayList<Coin> coins;

    private ArrayList<Explosion> explosions;

    //===============Methods=============//

    //constructor
    public GamePanel (Context context){
        //pass the context to the super classes constructor
        super(context);
        //add the callback to touch input
        getHolder().addCallback(this);
        //set game panel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //what will happen when user pressed return or home button on the android device
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000){
            counter++;
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    //only run once when open the game, build every thing up
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //read resource for the background
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.bc4));
        //get png picture for player         png picture, length of each frame, height of each frame, number of frames
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 26, 3);
        // create the empty array list of smoke puff behind player
        smoke = new ArrayList<SmokePuff>();
        //the array list of missile whch is empty for now as well
        missiles = new ArrayList<Missile>();
        //ASAA
        topBorders = new ArrayList<TopBorder>();
        //ASAA
        botBorders = new ArrayList<BotBorder>();

        coins = new ArrayList<Coin>();

        explosions = new ArrayList<Explosion>();

        smokeStartTime = System.nanoTime();

        missileStartTime = System.nanoTime();

        coinStartTime = System.nanoTime();

        bgm = MediaPlayer.create(getContext(), R.raw.nyancat);
        bgm.setVolume(0.15f,0.15f);
        firstGame = true;

        thread = new MainThread(getHolder(), this);
        //start game here
        //让画面开始刷新
        thread.setRunning(true);
        thread.start();

        //BGM start
        bgm.setLooping(true);
        bgm.start();

        explosionSound = MediaPlayer.create(getContext(), R.raw.bomb_exploding);
        tada = MediaPlayer.create(getContext(), R.raw.tada);
    }




    @Override
    public boolean onTouchEvent(MotionEvent event){
        //when touch the screen
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            //第一次按住开始游戏
            //first touch will start the game when game is not running but reset was complete
            if(!player.getPlaying() && newGameCreated && reset)
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            //第二次就要飞
            //second time hold start flying upward
            if(player.getPlaying())
            {
                //start the game if it's not started for some reason
                if(!started){
                    started = true;
                }
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        //不按就掉
        //player is moving down when screen is not touched
        if(event.getAction() == MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    //总的更新method 在里面叫别的更新
    //sum all other objects update
    public void update(){
        if(player.getPlaying()){

            if(botBorders.isEmpty()){
                player.setPlaying(false);
                return;
            }

            if(topBorders.isEmpty()){
                player.setPlaying(false);
                return;
            }

            firstGame = false;

            bg.update();
            player.update();
            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or min is met;

            maxBorderHeight = 30 + player.getScore()/progressDenom;

            //can't higher than half of screen

            if(maxBorderHeight > HEIGHT/4)
                maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            //check top border collision
            for(int i = 0; i < topBorders.size(); i++){
                if(collision(topBorders.get(i), player)){
                    player.setPlaying(false);
                }
            }
            //check bot border collision
            for(int i = 0; i < botBorders.size(); i++){
                if(collision(botBorders.get(i),player)){
                    player.setPlaying(false);
                }
            }

            //update top border
            this.updateTopBorder();

            //update bot border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime)/1000000;
            if(missileElapsed>(2000-player.getScore()/4)){
                //first missile always goes down the middle
                if(missiles.size()==0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missile),WIDTH+10,HEIGHT/2, 45,15,player.getScore(),13));
                }else{
                    //second missile is random
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missile),WIDTH+10, (int)(Math.random()*(HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight) , 45,15,player.getScore(),13));
                }
                missileStartTime = System.nanoTime();
            }
            //loop through every missile
            for(int i = 0; i < missiles.size();i++){
                missiles.get(i).update();
                if(collision(missiles.get(i), player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove when its way off screen
                if(missiles.get(i).getX() < -100){
                    missiles.remove(i);
                    break;
                }
            }


            //coins process
            long coinElapsed = (System.nanoTime() - coinStartTime)/1000000;
            if(coinElapsed>(3000)){
                //first coin always goes down the middle
                if(coins.size()==0){
                    coins.add(new Coin(BitmapFactory.decodeResource(getResources(), R.drawable.
                            coin),WIDTH+30,HEIGHT/2-30, 30,30,10));
                }else{
                    //second coin is random
                    coins.add(new Coin(BitmapFactory.decodeResource(getResources(), R.drawable.
                            coin),WIDTH+10, (int)(Math.random()*(HEIGHT - (maxBorderHeight * 2)-60) + maxBorderHeight) , 30,30,10));
                }
                coinStartTime = System.nanoTime();
            }

            //loop through every coin
            for(int i = 0; i < coins.size();i++){
                coins.get(i).update();
                if(collision(coins.get(i), player)){
                    tada.start();
                    coins.remove(i);
                    player.getCoin();
                    break;
                }
                //remove when its way off screen
                if(coins.get(i).getX() < -20){
                    coins.remove(i);
                    break;
                }
            }

            //the coin and missile collision
            for(int i = 0;i<missiles.size();i++){
                for(int j = 0; j < coins.size(); j++){
                    if(collision(missiles.get(i), coins.get(j) )){
                        explosions.add( new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), coins.get(j).getX()-30,coins.get(j).getY()-30,100,100,25,true));
                        explosionSound.start();
                        coins.remove(j);
                        missiles.remove(i);
                    }
                }
            }

            for(int i = 0; i<explosions.size();i++){
                explosions.get(i).update();
                if(explosions.get(i).done){
                    explosions.remove(i);
                }
            }

            //add smoke puff to timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new SmokePuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i < smoke.size(); i++){
                smoke.get(i).update();
                if(smoke.get(i).getX() < -10){
                    smoke.remove(i);
                }
            }
        }
        //player after stop playing most likely player died
        else{
            //only run once right after collision
            if(!reset){
                //just died and haven't begin creating new game yet
                newGameCreated = false;
                //when begin to reset; use to count down the delay time
                startReset = System.nanoTime();
                //reset is begin
                reset = true;
                //let player disappear
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),
                        player.getY()-30,100,100,25,false);
                if(!firstGame){
                    explosionSound.start();
                }
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset)/1000000;

            //pause for 2.5second after explosion
            if(((firstGame && resetElapsed > 1000) || resetElapsed > 3000) && !newGameCreated){
                newGame();
            }
        }
    }

    public boolean collision(GameObject a,  GameObject b){
        if(Rect.intersects(a.getRectangle(), b.getRectangle())){
            return true;
        }
        return false;
    }



    //用来让画面适应屏幕尺寸
    //use to scale the screen
    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas){
        //use devices resolution dived game res
        final float scaleFactorX = getWidth()/(float)WIDTH;
        final float scaleFactorY = getHeight()/(float)HEIGHT;

        //times game res by factor
        if(canvas!=null){
            final int savedState = canvas.save();

            canvas.scale(scaleFactorX,scaleFactorY);
            //and draw here
            bg.draw(canvas);
            if(!disappear){
                player.draw(canvas);
            }
            //draw smoke puff
            for(SmokePuff sp: smoke){
                sp.draw(canvas);
            }
            //draw missiles
            for(Missile m : missiles){
                m.draw(canvas);
            }
            //draw coins
            for(Coin c : coins){
                c.draw(canvas);
            }


            //draw topborder
            for(TopBorder tb: topBorders){
                tb.draw(canvas);
            }

            //draw botborder

            for(BotBorder bb: botBorders){
                bb.draw(canvas);
            }

            //draw explosion

            for(Explosion ex: explosions){
                ex.draw(canvas);

            }
            if(started){
                explosion.draw(canvas);
            }

            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }


    public void updateTopBorder()
    {
        for(int i = 0; i<topBorders.size(); i++)
        {
            topBorders.get(i).update();
            if(topBorders.get(i).getX()<-20)
            {
                topBorders.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if(topBorders.get(topBorders.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                if(topBorders.get(topBorders.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                //new border added will have larger height
                if(topDown)
                {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topBorders.get(topBorders.size()-1).getX()+20,
                            0, topBorders.get(topBorders.size()-1).getHeight()+1));
                }
                //new border added wil have smaller height
                else
                {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick),topBorders.get(topBorders.size()-1).getX()+20,
                            0, topBorders.get(topBorders.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder(){
        //update bottom border
        for(int i = 0; i<botBorders.size(); i++)
        {
            botBorders.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if(botBorders.get(i).getX()<-20) {
                botBorders.remove(i);
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botBorders.get(botBorders.size() - 1).getX() + 20,
                    HEIGHT - ((int)(Math.random()*(maxBorderHeight-minBorderHeight))+minBorderHeight)));
            }
        }
    }

    //last (third) part of reset. clear every thing except best scores
    public void newGame(){
        //the player can appear now
        disappear = false;
        //clean all the array lists
        botBorders.clear();
        topBorders.clear();
        missiles.clear();
        coins.clear();
        smoke.clear();
        explosions.clear();

        //reset value of borders
        minBorderHeight = 5;
        maxBorderHeight = 30;

        //check if did better then last time
        if(player.getScore()>best){
            best = player.getScore();
            better = true;
        }else{
            better = false;
        }

        //reset player position and score
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);
        //create the initial borders

        //top
        for(int i = 0; i *20<WIDTH + 40; i++){
            //first
            if(i==0){
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i*20, 0,10));
            }

            else{
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i*20, 0,topBorders.get(i-1).getHeight()+1));
            }
        }

        //bot
        for (int i = 0; i*20<WIDTH+40; i++){
            //first
            if(i == 0){
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i *20, HEIGHT - minBorderHeight));
            }
            //add more
            else{
                botBorders.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, botBorders.get(i - 1).getY()-1));
            }
        }

        newGameCreated = true;
    }

    //second part of reset
    public void drawText(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("Distance: " + (player.getScore()), 10,30,paint);
        canvas.drawText("Best: " + best, WIDTH - 215, 30, paint);


        if(!player.getPlaying() && newGameCreated && reset){
            Paint paint1 = new Paint();
            paint1.setColor(Color.BLACK);
            if(firstGame){
                paint1.setTextSize(40);
                paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("Press to start", WIDTH/2 - 50 , HEIGHT/2, paint1);

                paint1.setTextSize(20);
                canvas.drawText("Press and hold to go up", WIDTH/2-50, HEIGHT/2+20, paint1);
                canvas.drawText("Release to go down", WIDTH/2-50, HEIGHT/2+40, paint1);
            }else{
                paint1.setTextSize(40);
                paint1.setColor(Color.RED);
                paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("Press to retry", WIDTH/2 - 50 , HEIGHT/2, paint1);
                if(better){
                    paint1.setColor(Color.MAGENTA);
                    canvas.drawText("You are the best", WIDTH/2 - 50 , HEIGHT/2-40, paint1);
                }else{
                    canvas.drawText("You lose", WIDTH/2 - 50 , HEIGHT/2-40, paint1);

                }
                paint1.setColor(Color.BLACK);
                paint1.setTextSize(20);
                canvas.drawText("Press and hold to go up", WIDTH/2-50, HEIGHT/2+20, paint1);
                canvas.drawText("Release to go down", WIDTH/2-50, HEIGHT/2+40, paint1);
                canvas.drawText("Don't give up and try again", WIDTH/2-50, HEIGHT/2+60, paint1);

            }
        }
    }
}
