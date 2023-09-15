package com.myapp2;
import android.app.Activity;
import android.content.Context;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.rnbiometrics.CreateSignatureCallback;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


public class CalendarModule extends ReactContextBaseJavaModule implements TextToSpeech.OnInitListener {

    ReactApplicationContext myContext;
    private static TextToSpeech tts;
    private boolean isReady = false;
    private static Locale language = Locale.FRANCE;
    private static Executor executor;
    private static BiometricPrompt biometricPrompt;
    private static BiometricPrompt.PromptInfo promptInfo;

    CalendarModule(ReactApplicationContext context) {
        super(context);
        tts = new TextToSpeech(context, this);
        tts.setPitch(0.8f);
        tts.setSpeechRate(0.9f);
        this.myContext = context;
        executor = Executors.newSingleThreadExecutor();
    }

    @ReactMethod(isBlockingSynchronousMethod = true)
    @Override
    public String getName() {
        return "CalendarModule";
    }

    @ReactMethod
    public void callDeviceDiscovery(Callback successCallback, Callback errorCallback){
        try{
            successCallback.invoke("Called Device Discovery ");
        }catch (Exception e){
            errorCallback.invoke(e.getLocalizedMessage());
        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            tts.setLanguage(language);
            isReady = true;
        } else{
            isReady = false;
        }
    }
    @ReactMethod
    public void speak(String text){
        if(isReady) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
    }
    private boolean isCurrentSDKMarshmallowOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
    @ReactMethod
    public void BiometricPromptAndroid(final Callback successCallback, final Callback errorCallback){
        if (isCurrentSDKMarshmallowOrLater()) {
            UiThreadUtil.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
                                Executor executor = Executors.newSingleThreadExecutor();
                                biometricPrompt = new BiometricPrompt(fragmentActivity, executor, new BiometricPrompt.AuthenticationCallback() {
                                    @Override
                                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                                        super.onAuthenticationError(errorCode, errString);
                                        System.out.println("Erreur"+errString);
                                        errorCallback.invoke("Error during authentication");
                                    }
                                    @Override
                                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                                        super.onAuthenticationSucceeded(result);
                                        System.out.println("Bien");
                                        successCallback.invoke("Authentication validate successfully");
                                    }

                                    @Override
                                    public void onAuthenticationFailed() {
                                        super.onAuthenticationFailed();
                                        HashMap<String,String> JSON = null;
                                        errorCallback.invoke("Error during authentication");
                                    }

                                    @Override
                                    public int hashCode() {
                                        return super.hashCode();
                                    }

                                    @Override
                                    public boolean equals(@Nullable Object obj) {
                                        return super.equals(obj);
                                    }

                                    @NonNull
                                    @Override
                                    protected Object clone() throws CloneNotSupportedException {
                                        return super.clone();
                                    }

                                    @NonNull
                                    @Override
                                    public String toString() {
                                        return super.toString();
                                    }

                                    @Override
                                    protected void finalize() throws Throwable {
                                        super.finalize();
                                    }
                                });
                                promptInfo = new BiometricPrompt
                                        .PromptInfo
                                        .Builder()
                                        .setTitle("Biometric authentification")
                                        .setNegativeButtonText("Cancel")
                                        .build();
                                biometricPrompt.authenticate(promptInfo);
                            } catch (Exception e) {
                                errorCallback.invoke(e.getMessage());
                                System.out.println("Application Main"+e.getMessage());
                            }
                        }
                    });
        } else {
            HashMap<String,String> JSON = null;
            JSON.put("Error","This phone is not authorized");
            errorCallback.invoke("Error during authentication");
        }

    }

}
