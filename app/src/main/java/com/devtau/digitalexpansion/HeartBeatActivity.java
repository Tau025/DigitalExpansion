package com.devtau.digitalexpansion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.Spinner;

public class HeartBeatActivity extends AppCompatActivity {
    private ImageButton ibHeart;
    private AnimatorSet animatorSet;
    private Spinner spnFloatEvaluatorType, spnHeartBeatRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setExitTransition(new Slide(Gravity.LEFT));
        }

        //подготовим лэйаут и ссылки на нужные виджеты
        setContentView(R.layout.activity_heart_beat);
        ibHeart = (ImageButton) findViewById(R.id.ibHeart);
        spnFloatEvaluatorType = (Spinner) findViewById(R.id.spnFloatEvaluatorType);
        spnHeartBeatRate = (Spinner) findViewById(R.id.spnHeartBeatRate);

        //назначим стартовое состояние виджетам
        spnFloatEvaluatorType.setSelection(2);
        spnHeartBeatRate.setSelection(2);
    }

    @Override
    protected void onResume(){
        super.onResume();
        ibHeart.postDelayed(new Runnable(){
            @Override
            public void run() {
                animateHeartBeat(ibHeart);
                MyApplication.getRefWatcher(getApplicationContext()).watch(animatorSet);
            }
        }, Constants.VIEW_SHOW_UP_DELAY);
    }

    private void animateHeartBeat(final View animatedView) {
        int heartBeatRateBPM = getHeartBeatRate();//70 ударов в минуту - это 857ms на один полный цикл анимации

        //вычислим вспомогательные параметры длительности отдельных анимаций
        int pauseLength = Math.round((60f / heartBeatRateBPM) * 1000) -
                Constants.HEART_INFLATION_LENGTH - Constants.HEART_DEFLATION_LENGTH - Constants.HEART_RESTORE_LENGTH;

        //подготовим отдельные элементы анимации
        AnimatorSet inflate = createResizeAnim(animatedView, 0, Constants.HEART_INCREASE_TO,
                Constants.HEART_INFLATION_LENGTH, spnFloatEvaluatorType.getSelectedItemPosition());
        AnimatorSet deflate = createResizeAnim(animatedView, Constants.HEART_INCREASE_TO, Constants.HEART_DECREASE_TO,
                Constants.HEART_DEFLATION_LENGTH, 0);
        AnimatorSet restoreSize = createResizeAnim(animatedView, Constants.HEART_DECREASE_TO, 0,
                Constants.HEART_RESTORE_LENGTH, 0);

        //соберем агрегированную анимацию из подготовленных элементов
        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(inflate, deflate, restoreSize);
        animatorSet.setStartDelay(pauseLength);

        //назначим слушатель
        animatorSet.addListener(new AnimatorListenerAdapter() {
            //флаг isCanceled нужен, т.к. даже прервав анимацию вызовом animatorSet.cancel()
            //мы все равно увидим вызов onAnimationEnd
            private boolean isCanceled = false;
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isCanceled) {
                    //зацикливаем рекурсивным вызовом чтобы иметь возможность читать изменения спиннеров
                    animateHeartBeat(animatedView);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(Constants.LOG_TAG, "onHeartAnimationCancel");
                isCanceled = true;
            }
        });
        animatorSet.start();
    }

    private int getHeartBeatRate() {
        int heartBeatRateBPM = 70;
        switch (spnHeartBeatRate.getSelectedItemPosition()) {
            //можно было бы использовать ENUM, но это усложнит код + ENUM медленный
            case 0: heartBeatRateBPM = 10; break;
            case 1: heartBeatRateBPM = 30; break;
            case 2: heartBeatRateBPM = 40; break;
            case 3: heartBeatRateBPM = 70; break;
            case 4: heartBeatRateBPM = 110; break;
        }
        return heartBeatRateBPM;
    }

    private AnimatorSet createResizeAnim(View animatedView, float fromDeltaScale, float toDeltaScale,
                                         final int duration, final int floatEvaluatorType) {
        //подготовим отдельные элементы анимации
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(animatedView, "scaleX", 1 + fromDeltaScale, 1 + toDeltaScale);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(animatedView, "scaleY", 1 + fromDeltaScale, 1 + toDeltaScale);

        //подготовим FloatEvaluator для ease-out эффекта
        FloatEvaluator floatEvaluator = new FloatEvaluator() {
            @Override
            public Float evaluate(float fraction, Number startValue, Number endValue) {
                float t = duration * fraction;
                float b = startValue.floatValue();
                float c = endValue.floatValue() - b;
                return calculate(t, b, c, duration);
            }

            //больше вариантов подходящих уравнений тут http://gizma.com/easing/
            private float calculate(float t, float b, float c, float d){
                float result = 0;
                switch (floatEvaluatorType) {
                    //можно было бы использовать ENUM, но это усложнит код + ENUM медленный
                    case 1://QUADRATIC
                        t /= d;
                        result = -c * t * (t - 2) + b;
                        break;
                    case 2://CUBIC
                        t /= d;
                        t--;
                        result =  c*(t*t*t + 1) + b;
                        break;
                    case 3://EXPONENTIAL
                        result = (float) (c * ( -Math.pow( 2, -10 * t/d ) + 1 ) + b);
                        break;
                }
                return result;
            }
        };

        if (floatEvaluatorType != 0) {
            //для создания эффекта сокращающихся желудочков применим FloatEvaluator только к ординате
            scaleUpY.setEvaluator(floatEvaluator);
//            scaleUpY.setInterpolator(new DecelerateInterpolator());//скучно
        }

        //соберем результат из подготовленных частей
        AnimatorSet resize = new AnimatorSet();
        resize.playTogether(scaleUpX, scaleUpY);
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
