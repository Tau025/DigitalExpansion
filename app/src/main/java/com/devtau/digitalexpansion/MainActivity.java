package com.devtau.digitalexpansion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private ImageButton ibMessage;
    private AnimatorSet animatorSet;
    //переменная для страховки от двойного нажатия кнопки перехода
    private boolean transitionToHBAStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //установим анимацию перехода только между двумя существующими активностями
        //на случай, если к новым активностям нужно будет применять другие анимации
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setExitTransition(new Slide(Gravity.LEFT));
        }

        setContentView(R.layout.activity_main);
        ibMessage = (ImageButton) findViewById(R.id.ibMessage);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //покажем еще одну приятную анимацию появления нашей кнопки на экране (бонус к тз)
        ibMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 21) {
                    showViewCircular(ibMessage, 600);
                } else {
                    showView(ibMessage, 600);
                }
            }
        }, 200);
        transitionToHBAStarted = false;
    }

    @TargetApi(21)
    private void showViewCircular(View myView, long duration){
        if (myView != null) {
            myView.setAlpha(1f);
            myView.setVisibility(View.VISIBLE);
            int cx = myView.getWidth() / 2;
            int cy = myView.getHeight() / 2;
            float finalRadius = calculateRadius(myView);
            Log.d(Constants.LOG_TAG, "cx: " + String.valueOf(cx) + "cy: " + String.valueOf(cy) + "finalRadius: " + String.valueOf(finalRadius));
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
            anim.setDuration(duration);
            anim.start();
        }
    }

    private void showView(View myView, long duration){
        if (myView != null) {
            myView.setAlpha(1f);
            myView.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha", 0, 1f);
            fadeIn.setDuration(duration);
            fadeIn.start();
        }
    }

    private float calculateRadius(View view) {
        return (float) Math.sqrt(view.getWidth() * view.getWidth() + view.getHeight() * view.getHeight()) / 2;
    }

    public void onMsgIconClick(View view) {
        int viewRadius = Math.round(calculateRadius(view));
        int distance = (view.getLeft() + view.getRight()) / 2 + viewRadius;
        animate(view, 600, 0, -distance, 0, -270, 1, 0);
    }

    private void animate(final View animatedView, int duration, int fromXDelta, int toXDelta,
                         float fromAngle, final float toAngle,
                         float fromTransparency, final float toTransparency) {
        //подготовим отдельные элементы анимации
        ObjectAnimator moverX = ObjectAnimator.ofFloat(animatedView, "translationX", (float) fromXDelta, (float) toXDelta);
        ObjectAnimator rotate = ObjectAnimator.ofFloat(animatedView, "rotation", fromAngle, toAngle);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(animatedView, "alpha", fromTransparency, toTransparency);

        //соберем агрегированную анимацию из подготовленных элементов
        animatorSet = new AnimatorSet();
        animatorSet.play(moverX).with(rotate).with(fadeOut);
        animatorSet.setDuration(duration);

        //назначим слушатель
        animatorSet.addListener(new AnimatorListenerAdapter() {
            //флаг isCanceled нужен, т.к. даже прервав анимацию вызовом animatorSet.cancel()
            //мы все равно увидим вызов onAnimationEnd
            private boolean isCanceled = false;
            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(Constants.LOG_TAG, "onAnimationEnd");

                //необходимо для корректного завершения анимации перемещения
                animator = ObjectAnimator.ofFloat(animatedView, "translationX", 0.0f, 0.0f);
                animator.setDuration(1);
                animator.start();

                //установим конечные значения положения и прозрачности после выполнения анимации
                animatedView.setAlpha(toTransparency);
                animatedView.setVisibility(View.INVISIBLE);
                animatedView.setRotation(0);

                //запускаем вторую активность только если анимацию не прервали и со страховкой от трясущихся рук
                if (!isCanceled && !transitionToHBAStarted) {
                    startSecondActivity();
                    transitionToHBAStarted = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(Constants.LOG_TAG, "onAnimationCancel");
                isCanceled = true;
            }
        });
        animatorSet.start();
    }

    private void startSecondActivity() {
        Intent intent = new Intent(this, HeartBeatActivity.class);
        //вызовем анимированный переход ко второй активности, если это позволяет апилвл устройства
        if (Build.VERSION.SDK_INT >= 21) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //прервем анимацию
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }
}
