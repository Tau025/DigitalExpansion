package com.devtau.digitalexpansion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Spinner;
/**
 * Created by TAU on 30.05.2016.
 */
public class MyAnimator {
    private AnimatorSet animatorSet;
    //переменная для страховки от двойного нажатия кнопки перехода
    private boolean transitionToHBAStarted = false;
    private float incrementalValue = 0f;

    public void animateShowUpCircularOrFadeIn(View animatedView) {
        if (Build.VERSION.SDK_INT >= 21) {
            showViewCircular(animatedView);
        } else {
            showView(animatedView);
        }
    }

    @TargetApi(21)
    private void showViewCircular(View animatedView){
        if (animatedView != null) {
            animatedView.setAlpha(1f);
            animatedView.setVisibility(View.VISIBLE);
            int cx = animatedView.getWidth() / 2;
            int cy = animatedView.getHeight() / 2;
            float finalRadius = calculateRadius(animatedView);
            Animator anim = ViewAnimationUtils.createCircularReveal(animatedView, cx, cy, 0, finalRadius);
            anim.setDuration(Constants.CIRCLE_ANIMATION_LENGTH);
            anim.start();
        }
    }

    private float calculateRadius(View animatedView) {
        return (float) Math.sqrt(animatedView.getWidth() * animatedView.getWidth()
                + animatedView.getHeight() * animatedView.getHeight()) / 2;
    }

    private void showView(View animatedView){
        if (animatedView != null) {
            animatedView.setAlpha(1f);
            animatedView.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(animatedView, "alpha", 0, 1f);
            fadeIn.setDuration(Constants.CIRCLE_ANIMATION_LENGTH);
            fadeIn.start();
        }
    }

    public void animateComplexSlide(final View animatedView, int duration,
                                     float fromAngle, final float toAngle,
                                     float fromTransparency, final float toTransparency, final Activity activity) {
        //вычислим смещение animatedView по абсциссе
        int viewRadius = Math.round(calculateRadius(animatedView));
        final int toXDelta = -((animatedView.getLeft() + animatedView.getRight()) / 2 + viewRadius);//+ вправо - влево

        //подготовим отдельные элементы анимации
        ValueAnimator mover = ObjectAnimator.ofFloat(animatedView, "translationX", 0, (float) toXDelta);
        //назначим слушатель, рассчитывающий и применяющий скольжение по траектории
        mover.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) (animation.getAnimatedValue());
                animatedView.setTranslationX(value + incrementalValue);
                animatedView.setTranslationY(-(float) (Constants.SINUSOID_AMPLITUDE *
                        Math.sin(value / toXDelta * Math.PI * Constants.SINUSOID_LENGTH)));
                incrementalValue += Constants.SLIDE_ANIMATION_STEP;
            }
        });
        ObjectAnimator rotate = ObjectAnimator.ofFloat(animatedView, "rotation", fromAngle, toAngle);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(animatedView, "alpha", fromTransparency, toTransparency);

        //соберем агрегированную анимацию из подготовленных элементов
        animatorSet = new AnimatorSet();
        animatorSet.play(mover).with(rotate).with(fadeOut);
        animatorSet.setDuration(duration);

        //назначим слушатель для агрегированной анимации
        animatorSet.addListener(new AnimatorListenerAdapter() {
            //флаг isCanceled нужен, т.к. даже прервав анимацию вызовом animatorSet.cancel()
            //мы все равно увидим вызов onAnimationEnd
            private boolean isCanceled = false;
            @Override
            public void onAnimationEnd(Animator animator) {
                Log.d(Constants.LOG_TAG, "onSlideAnimationEnd");

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
                    startSecondActivity(activity);
                    transitionToHBAStarted = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d(Constants.LOG_TAG, "onSlideAnimationCancel");
                isCanceled = true;
            }
        });
        animatorSet.start();
    }

    private void startSecondActivity(Activity activity) {
        Intent intent = new Intent(activity, HeartBeatActivity.class);
        //вызовем анимированный переход ко второй активности, если это позволяет апилвл устройства
        if (Build.VERSION.SDK_INT >= 21) {
            activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public void setTransitionToHBAStarted(boolean transitionToHBAStarted) {
        this.transitionToHBAStarted = transitionToHBAStarted;
    }

    public void animateHeartBeat(final View animatedView, final Spinner spnFloatEvaluatorType, final Spinner spnHeartBeatRate) {
        int heartBeatRateBPM = getHeartBeatRate(spnHeartBeatRate);//70 ударов в минуту - это 857ms на один полный цикл анимации

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
            public void onAnimationEnd(android.animation.Animator animator) {
                if (!isCanceled) {
                    //зацикливаем рекурсивным вызовом чтобы иметь возможность читать изменения спиннеров
                    animateHeartBeat(animatedView, spnFloatEvaluatorType, spnHeartBeatRate);
                }
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                Log.d(Constants.LOG_TAG, "onHeartAnimationCancel");
                isCanceled = true;
            }
        });
        animatorSet.start();
    }

    private int getHeartBeatRate(Spinner spnHeartBeatRate) {
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

    public void stopAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }
}
