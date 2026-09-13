package dev.screentime.patches

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val RUNTIME_PACKAGE = "dev.screentime.runtime"
private const val ORIGINAL_FACTORY_METADATA = "dev.screentime.ORIGINAL_COMPONENT_FACTORY"

/** Adds runtime declarations without assuming a target package or version. */
private val screenTimeManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: error("Target manifest has no application element")
            val originalFactory = application.getAttributeNS(ANDROID_NS, "appComponentFactory")
            if (originalFactory.isNotEmpty()) ensureMetaData(document, application, ORIGINAL_FACTORY_METADATA, originalFactory)
            application.setAttributeNS(ANDROID_NS, "android:appComponentFactory", "$RUNTIME_PACKAGE.DelegatingAppComponentFactory")
            ensureComponent(document, application, "activity", "$RUNTIME_PACKAGE.ScreenTimeActivity") {
                setAndroidAttribute("exported", "false")
                setAndroidAttribute("process", ":screentime_control")
                setAndroidAttribute("excludeFromRecents", "true")
            }
            ensureComponent(document, application, "provider", "$RUNTIME_PACKAGE.StateProvider") {
                val packageName = document.documentElement.getAttribute("package")
                require(packageName.isNotEmpty()) { "Target manifest has no package name" }
                setAndroidAttribute("authorities", "$packageName.screentime.internal")
                setAndroidAttribute("exported", "false")
                setAndroidAttribute("process", ":screentime_control")
            }
        }
    }
}

/** The single user-visible entry point. It is opt-in because universal compatibility is unverified. */
@Suppress("unused")
val screenTimePatch = bytecodePatch(
    name = "Screen time",
    description = "Experimental per-app screen-time controls with a local enforcement runtime.",
    default = false
) {
    dependsOn(screenTimeManifestPatch)
    extendWith("extensions/extension.mpe")
    execute { }
}

private fun ensureMetaData(document: Document, application: Element, name: String, value: String) {
    val existing = application.getElementsByTagName("meta-data")
    for (index in 0 until existing.length) {
        val element = existing.item(index) as Element
        if (element.getAttributeNS(ANDROID_NS, "name") == name) return
    }
    application.appendChild(document.createElement("meta-data").apply {
        setAndroidAttribute("name", name)
        setAndroidAttribute("value", value)
    })
}

private fun ensureComponent(document: Document, application: Element, tagName: String, className: String, configure: Element.() -> Unit) {
    val existing = application.getElementsByTagName(tagName)
    for (index in 0 until existing.length) {
        val element = existing.item(index) as Element
        if (element.getAttributeNS(ANDROID_NS, "name") == className) return
    }
    application.appendChild(document.createElement(tagName).apply {
        setAndroidAttribute("name", className)
        configure()
    })
}

private fun Element.setAndroidAttribute(name: String, value: String) = setAttributeNS(ANDROID_NS, "android:$name", value)
