package com.devtau.digitalexpansion;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;

public class HeartBeatActivity extends AppCompatActivity {
    private ImageButton ibHeart;
    private Spinner spnFloatEvaluatorType, spnHeartBeatRate;
    private MyAnimator animator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
            getWindow().setExitTransition(new Slide(Gravity.LEFT));
        }

        setContentView(R.layout.activity_heart_beat);
        initiateControls();
        animator = new MyAnimator();
        MyApplication.getRefWatcher(getApplicationContext()).watch(animator);
    }

    private void initiateControls() {
        //подготовим ссылки на нужные виджеты
        ibHeart = (ImageButton) findViewById(R.id.ibHeart);
        spnFloatEvaluatorType = (Spinner) findViewById(R.id.spnFloatEvaluatorType);
        spnHeartBeatRate = (Spinner) findViewById(R.id.spnHeartBeatRate);

        //назначим стартовое состояние спиннерам
        spnFloatEvaluatorType.setSelection(2);
        spnHeartBeatRate.setSelection(2);
    }

    @Override
    protected void onResume(){
        super.onResume();
        ibHeart.postDelayed(new Runnable(){
            @Override
            public void run() {
                animator.animateHeartBeat(ibHeart, spnFloatEvaluatorType, spnHeartBeatRate);
            }
        }, Constants.VIEW_SHOW_UP_DELAY);
    }

    public void onHeartIconClick(View view) {
        animator.stopAnimation();
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        animator.stopAnimation();
    }
}
