package saiboten.no.synclistener.dagger;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import dagger.ObjectGraph;

public class BaseApplication extends Application {
    private ObjectGraph graph;

    @Override
    public void onCreate() {
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