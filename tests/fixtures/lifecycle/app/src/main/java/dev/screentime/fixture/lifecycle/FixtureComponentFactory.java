package dev.screentime.fixture.lifecycle;

import android.app.AppComponentFactory;
import android.app.Application;

/** Deliberately present so the screen-time delegating factory can be checked for preservation. */
public final class FixtureComponentFactory extends AppComponentFactory {
    @Override public Application instantiateApplication(ClassLoader loader, String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.instantiateApplication(loader, name);
    }
}
