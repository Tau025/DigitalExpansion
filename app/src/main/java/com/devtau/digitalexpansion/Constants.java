package com.devtau.digitalexpansion;

/**
 * Created by TAU on 20.05.2016.
 */
public abstract class Constants {
    public static final String LOG_TAG = "MY_LOG";

    public static final float SINUSOID_AMPLITUDE = 80f;
    public static final int SINUSOID_LENGTH = 2;//полуоборотов
    public static final int VIEW_SHOW_UP_DELAY = 200;
    public static final int CIRCLE_ANIMATION_LENGTH = 600;
    public static final int SLIDE_ANIMATION_DURATION = 600;
    public static final float SLIDE_ANIMATION_STEP = 1f;
    public static final int ROTATION_ANIMATION_TARGET_ANGLE = -270;

    //вводные данные, определяющие анимацию сердцебиения
    public static final int HEART_INFLATION_LENGTH = 150;//50 ближе к реалистичности
    public static final int HEART_DEFLATION_LENGTH = 240;//85 ближе к реалистичности
    public static final int HEART_RESTORE_LENGTH = 150;//50 ближе к реалистичности
    public static final float HEART_INCREASE_TO = 0.4f;//0.2 ближе к реалистичности
    public static final float HEART_DECREASE_TO = -0.06f;//-0.03 ближе к реалистичности
}
