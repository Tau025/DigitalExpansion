package com.devtau.digitalexpansion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

public class HeartBeatActivity extends AppCompatActivity {
    private ImageButton ibHeart;
    private AnimatorSet animatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setExitTransition(new Slide(Gravity.LEFT));
        }

        setContentView(R.layout.activity_heart_beat);
        ibHeart = (ImageButton) findViewById(R.id.ibHeart);
        MyApplication.getRefWatcher(getApplicationContext()).watch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ibHeart.postDelayed(new Runnable(){
            @Override
            public void run() {
                animateHeartBeat(ibHeart, 70);
            }
        }, 700);
    }

    private void animateHeartBeat(final View animatedView, int heartBeatRateBPM) {
        //вводные данные, определяющие анимацию сердцебиения
        //heartBeatRateBPM = 70 ударов в минуту - это 857ms на один полный цикл анимации
        float increaseSize = 0.15f;
        float decreaseSize = 0.03f;

        //вычислим вспомогательные параметры длительности отдельных анимаций
        int inflationLength = 70 / heartBeatRateBPM * 50;
        int deflationLength = 70 / heartBeatRateBPM * 85;
        int restoreSizeLength = 70 / heartBeatRateBPM * 50;
        final int pauseLength = 70 / heartBeatRateBPM * 672;

        //подготовим отдельные элементы анимации
        AnimatorSet inflate = createResizeAnim(animatedView, 0, increaseSize, inflationLength);
        AnimatorSet deflate = createResizeAnim(animatedView, increaseSize, -decreaseSize, deflationLength);
        AnimatorSet restoreSize = createResizeAnim(animatedView, -decreaseSize, 0, restoreSizeLength);

        //соберем агрегированную анимацию из подготовленных элементов
        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(inflate, deflate, restoreSize);

        //назначим слушатель
        animatorSet.addListener(new AnimatorListenerAdapter() {
            //флаг isCanceled нужен, т.к. даже прервав анимацию вызовом animatorSet.cancel()
            //мы все равно увидим вызов onAnimationEnd
            private boolean isCanceled = false;
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isCanceled) {
                    animatorSet.setStartDelay(pauseLength);
                    animatorSet.start();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCanceled = true;
            }
        });
        animatorSet.start();

        MyApplication.getRefWatcher(getApplicationContext()).watch(animatorSet);
    }

    private AnimatorSet createResizeAnim(View animatedView, float fromDeltaScale, float toDeltaScale, int duration) {
        AnimatorSet resize = new AnimatorSet();
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(animatedView, "scaleX", 1 + fromDeltaScale, 1 + toDeltaScale);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(animatedView, "scaleY", 1 + fromDeltaScale, 1 + toDeltaScale);
        resize.play(scaleUpX).with(scaleUpY);
        resize.setDuration(duration);
        return resize;
    }

    public void onHeartIconClick(View view) {
        //прервем анимацию
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        super.onBackPressed();
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
