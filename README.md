F-Droid Client
==============

Client for [F-Droid](https://fdroid.org), the Free Software repository system
for Android.

Building from source with Gradle
--------------------------------

The only required tools are the [Android SDK](http://developer.android.com/sdk/index.html) and Gradle.

You should use a relatively new version of Gradle, such as 2.4, or use the
gradle wrapper.

Once you have checked out the version you wish to build, run:

```
git submodule update --init
cd F-Droid
gradle clean assembleRelease
```

Android Studio
--------------

From Android Studio: File -> Import Project -> Select the cloned top folder


Direct download
---------------

You can [download the application](https://f-droid.org/FDroid.apk) directly
from our site or [browse it in the
repo](https://f-droid.org/app/org.fdroid.fdroid).


Contributing
------------

You are welcome to submit
[Merge Requests](https://gitlab.com/fdroid/fdroidclient/merge_requests)
via the Gitlab web interface. You can also follow our
[Issue tracker](https://f-droid.org/repository/issues/) and our
[Forums](https://f-droid.org/forums/).


Translating
-----------

The `res/values-*` dirs are kept up to date automatically via [MediaWiki's
Translate Extension](http://www.mediawiki.org/wiki/Extension:Translate). See
[our translation page](https://f-droid.org/wiki/page/Special:Translate) if you
would like to contribute.


Running the test suite
----------------------

In order to run the F-Droid test suite, you will need to have either a real device
connected via `adb`, or an emulator running. Then, execute the following from the
command line:

> `gradle connectedAndroidTest`

This will build and install F-Droid and the test apk, then execute the entire
test suite on the device or emulator.

See the [Android Gradle user guide](http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Testing) for more details, including how to use Android Studio to run tests (which provides
more useful feedback than the command line).


License
-------

This program is Free Software: You can use, study share and improve it at your
will. Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License](https://www.gnu.org/licenses/gpl.html) as
published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Some icons are made by [Picol](http://www.flaticon.com/authors/picol),
[Icomoon](http://www.flaticon.com/authors/icomoon) or
[Dave Gandy](http://www.flaticon.com/authors/dave-gandy) from
[Flaticon](http://www.flaticon.com) or by Google and are licensed by
[Creative Commons BY 3.0](http://creativecommons.org/licenses/by/3.0/).

Other icons are from the
[Material Design Icon set](https://github.com/google/material-design-icons)
released under an
[Attribution 4.0 International license](http://creativecommons.org/licenses/by/4.0/).
