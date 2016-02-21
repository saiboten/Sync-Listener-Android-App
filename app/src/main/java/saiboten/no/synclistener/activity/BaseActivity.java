package saiboten.no.synclistener.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import saiboten.no.synclistener.dagger.BaseApplication;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseApplication) getApplication()).inject(this);
    }
}
