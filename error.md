Отправить отчет

java.lang.IllegalArgumentException: width and height must be > 0
    at android.graphics.Bitmap.createBitmap(Bitmap.java:1299)
    at android.graphics.Bitmap.createBitmap(Bitmap.java:1265)
    at android.graphics.Bitmap.createBitmap(Bitmap.java:1213)
    at android.graphics.Bitmap.createBitmap(Bitmap.java:1172)
    at com.android.packageinstaller.PackageUtil$AppSnippet.writeToParcel(go/retraceme bba133826d292ce04e642b772e07337a1bf035484766d5b9f811c43c062fd73c:22)
    at android.os.Parcel.writeParcelable(Parcel.java:2869)
    at android.os.Parcel.writeValue(Parcel.java:2770)
    at android.os.Parcel.writeValue(Parcel.java:2647)
    at android.os.Parcel.writeArrayMapInternal(Parcel.java:1511)
    at android.os.BaseBundle.writeToParcelInner
    # Отправить отчет

android.os.BaseBundle.writeToParcelInner(BaseBundle.java:1874)
    at android.os.Bundle.writeToParcel(Bundle.java:1557)
    at android.os.Parcel.writeBundle(Parcel.java:1580)
    at android.content.Intent.writeToParcel(Intent.java:12717)
    at android.os.Parcel.writeTypedObject(Parcel.java:2488)
    at android.app.IActivityTaskManager$Stub$Proxy.startActivity(IActivityTaskManager.java:3036)
    at android.app.Instrumentation.execStartActivity(Instrumentation.java:2020)
    at android.app.Activity.startActivityForResult(Activity.java:6155)
    at android.app.Activity.startActivityForResult(Activity.java:6112)
    at android.app.Activity.startActivity(Activity.java:6615)
    at android.app.Activity.startActivity(Activity.java:6582)
    at com.android.packageinstaller.PackageInstallActivity.startInstall(com.android.packageinstaller/Activity.java:6582)

    # Отправить отчет

android.app.Activity.startActivity(Activity.java:6582)
    at com.android.packageinstaller.PackageInstallActivity.startInstall(go/retraceme
b1a133826d292ce04e642b772e07337a1bf035484766d5b9f811c43c062fd73c:114)
    at com.android.packageinstaller.PackageInstallActivity$
$ExternalSyntheticLambda0.onClick(go/retraceme
b1a133826d292ce04e642b772e07337a1bf035484766d5b9f811c43c062fd73c:44)
    at com.android.internal.app.AlertController$ButtonHandler.handleMessage(AlertController.java:186)
    at android.os.Handler.dispatchMessage(Handler.java:110)
    at android.os.Looper.loopOnce(Looper.java:267)
    at android.os.Looper.loop(Looper.java:360)
    at android.app.ActivityThread.main(ActivityThread.java:10054)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:616)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1115)

---

**Отмена**  
**Отправить**