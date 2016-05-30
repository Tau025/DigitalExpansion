package com.devtau.digitalexpansion;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
    private ImageButton ibMessage;
    private MyAnimator animator;

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
        animator = new MyAnimator();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //покажем еще одну приятную анимацию появления нашей кнопки на экране (бонус к тз)
        ibMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                animator.animateShowUpCircularOrFadeIn(ibMessage);
            }
        }, Constants.VIEW_SHOW_UP_DELAY);
        animator.setTransitionToHBAStarted(false);
    }

    public void onMsgIconClick(View view) {
        animator.animateComplexSlide(view, Constants.SLIDE_ANIMATION_DURATION,
                0, Constants.ROTATION_ANIMATION_TARGET_ANGLE, 1, 0, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        animator.stopAnimation();
    }
}
