package saiboten.no.synclistener.dagger;


import android.app.Application;
import dagger.ObjectGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseApplication extends Application {
    private ObjectGraph graph;

    @Override public void onCreate() {
        super.onCreate();

        graph = ObjectGraph.create(getModules().toArray());
    }

    protected List<Object> getModules() {

        List moduleList = new ArrayList();
        moduleList.add(new DaggerModule(this));

        return moduleList;
    }

    public void inject(Object object) {
        graph.inject(object);
    }
}