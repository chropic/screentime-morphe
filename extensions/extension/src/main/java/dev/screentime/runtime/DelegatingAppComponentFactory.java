package dev.screentime.runtime;

import android.app.Activity;
import android.app.AppComponentFactory;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;

/**
 * Factory wrapper experiment. The manifest patch records the target's factory name as metadata;
 * Android supplies ApplicationInfo at the class-loader hook before component construction.
 */
public final class DelegatingAppComponentFactory extends AppComponentFactory {
    private static final String ORIGINAL_FACTORY_METADATA = "dev.screentime.ORIGINAL_COMPONENT_FACTORY";
    private volatile AppComponentFactory delegate;

    @Override
    public ClassLoader instantiateClassLoader(ClassLoader classLoader, ApplicationInfo applicationInfo) {
        AppComponentFactory original = originalFactory(classLoader, applicationInfo == null ? null : applicationInfo.metaData);
        return original == null ? super.instantiateClassLoader(classLoader, applicationInfo)
            : original.instantiateClassLoader(classLoader, applicationInfo);
    }

    @Override
    public Application instantiateApplication(ClassLoader classLoader, String className)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AppComponentFactory original = delegate;
        return original == null ? super.instantiateApplication(classLoader, className)
            : original.instantiateApplication(classLoader, className);
    }

    @Override
    public Activity instantiateActivity(ClassLoader classLoader, String className, Intent intent)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AppComponentFactory original = delegate;
        return original == null ? super.instantiateActivity(classLoader, className, intent)
            : original.instantiateActivity(classLoader, className, intent);
    }

    @Override
    public Service instantiateService(ClassLoader classLoader, String className, Intent intent)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AppComponentFactory original = delegate;
        return original == null ? super.instantiateService(classLoader, className, intent)
            : original.instantiateService(classLoader, className, intent);
    }

    @Override
    public BroadcastReceiver instantiateReceiver(ClassLoader classLoader, String className, Intent intent)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AppComponentFactory original = delegate;
        return original == null ? super.instantiateReceiver(classLoader, className, intent)
            : original.instantiateReceiver(classLoader, className, intent);
    }

    @Override
    public ContentProvider instantiateProvider(ClassLoader classLoader, String className)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        AppComponentFactory original = delegate;
        return original == null ? super.instantiateProvider(classLoader, className)
            : original.instantiateProvider(classLoader, className);
    }

    private AppComponentFactory originalFactory(ClassLoader classLoader, Bundle metadata) {
        if (delegate != null || metadata == null) return delegate;
        String className = metadata.getString(ORIGINAL_FACTORY_METADATA);
        if (className == null || className.isEmpty() || className.equals(getClass().getName())) return null;
        try {
            Object candidate = classLoader.loadClass(className).getDeclaredConstructor().newInstance();
            if (!(candidate instanceof AppComponentFactory)) throw new IllegalStateException(className + " is not an AppComponentFactory");
            delegate = (AppComponentFactory) candidate;
            return delegate;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to preserve target AppComponentFactory " + className, exception);
        }
    }
}
