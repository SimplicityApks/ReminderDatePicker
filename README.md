*Date and time pickers have always been a hassle for me (no matter how awesome they looked). Having 
to choose from a few hundred numbers felt overwhelming and was neither intuitive nor fast. Luckily, 
Google found a sleek solution in the Notes app which I decided to replicate:*

ReminderDatePicker
==================

An intuitive and simplistic **Date and Time Picker for reminders**. Per default it almost exactly 
matches the picker seen in the [Google Notes App](https://play.google.com/store/apps/details?id=com.google.android.keep) 
but adds important features and enhancements. You can **download the [sample app in the Play Store here](https://play.google.com/store/apps/details?id=com.simplicityapks.reminderdatepicker.sample)** to test it!

Screenshots
-----------

<table border="0">
  <colgroup>
    <col width="*">
    <col width="*">
    </colgroup>
  <tr>
    <td align="center"><img src="https://raw.github.com/simplicityapks/ReminderDatePicker/screenshots/screenshot_date.png"></td>
    <td align="center"><img src="https://raw.github.com/simplicityapks/ReminderDatePicker/screenshots/screenshot_time.png"></td>
  </tr>
</table>

Set-up
------

To use this library in your project either

* Add this line to the dependencies in your build.gradle (note you need `mavenCentral()` in your repositories):
`compile 'com.simplicityapks:reminderdatepicker:1.1.+'`

* If you want the KitKat version (1.0) instead, use `compile 'com.simplicityapks:reminderdatepicker:1.0.+'`

* Copy the library into your workspace and [add it as library in Eclipse](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject) 
or add `compile project(':lib')` to the dependencies in your build.gradle in Android Studio.
In Eclipse, you will need to right click on the java folder in this project and select Build Path-->Use as Source Folder.
Also, make sure you have configured both the [appcompat support (v7) library](http://developer.android.com/tools/support-library/features.html#v7)
and the [DateTimePicker](https://github.com/flavienlaurent/datetimepicker) correctly and referenced as library.

The library is designed for Android ICS and above (API level 14+), but it works fine in Android 2.1+ 
as well (only the spinners use a dialog instead of the popup menu). Note that you need to use [NineOldAndroids](https://github.com/JakeWharton/NineOldAndroids/)
on pre-Honeycomb for the additional [DateTimePicker](https://github.com/flavienlaurent/datetimepicker) 
that is integrated out of the box.

Usage
-----

Simply construct a new [ReminderDatePicker](/lib/src/main/java/com/simplicityapks/reminderdatepicker/lib/ReminderDatePicker.java) 
or add it to your layout xml files:

    <com.simplicityapks.reminderdatepicker.lib.ReminderDatePicker
        android:id="@+id/date_picker"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"/>
        
See the [sample source code](https://github.com/SimplicityApks/ReminderDatePicker/tree/master/sample/src/main) for more information.

###Flags and Modes

This library supports different behaviours and selectable dates. Based on your needs, call `setFlags()` 
or use the `app:flags="..."` attribute in the xml declaration with one or more of the 
*ReminderDatePicker.FLAG_...* or *.MODE_...* constants (combined with the `|` operator). For example 
use `app:flags="month|more_time"` for a larger set of selectable days and hours. 

###Custom additional date or time picker

After clicking on the footer in one of the spinners, the additional date or time picker will open as 
dialog. These are the standard pickers from the Android framework, made Android 2.1+ compatible by 
[DateTimePicker](https://github.com/flavienlaurent/datetimepicker). You can, however, use your a custom
date or time picker or even implement your own behaviour. To achieve this simply call `setCustomDatePicker()` 
or `setCustomTimePicker()`, passing an OnClickListener whose *onClick()* method will be called when the 
footer is clicked.

###Advanced Usage

[DateSpinner](/lib/src/main/java/com/simplicityapks/reminderdatepicker/lib/DateSpinner.java) and 
[TimeSpinner](/lib/src/main/java/com/simplicityapks/reminderdatepicker/lib/TimeSpinner.java) can be 
used separately (when FLAG_HIDE_TIME does not suffice) and each listen for the xml flags as well.

Alternatively, if you have a totally different usage and need the spinners used here, let your custom 
Spinner extend [PickerSpinner](/lib/src/main/java/com/simplicityapks/reminderdatepicker/lib/PickerSpinner.java) 
(which uses a [PickerSpinnerAdapter](/lib/src/main/java/com/simplicityapks/reminderdatepicker/lib/PickerSpinnerAdapter.java)).
That way your spinner will have a footer, secondary texts and the ability to have a temporary selection.
The class also allows easy dynamic changes to the spinner items without having to check and reset 
the selection.

Credits
-------

**Huge thanks to**
* **Google** for the original design of this picker in Google Keep
* **[flavienlaurent](https://github.com/flavienlaurent)** for his awesome [DateTimePicker](https://github.com/flavienlaurent/datetimepicker)
* **[chrisbanes](https://github.com/chrisbanes)** for his [maven_push.gradle script](http://chris.banes.me/2013/08/27/pushing-aars-to-maven-central/) which brought this lib to maven central
* **[Oxygen Team](http://www.iconarchive.com/artist/oxygen-icons.org.html)** for designing the [icon of the sample app](http://www.iconarchive.com/show/oxygen-icons-by-oxygen-icons.org/Apps-preferences-system-time-icon.html)
* **[dancer_69](http://forum.xda-developers.com/member.php?u=390873)** for the Greek translation
* **[dahool](https://github.com/dahool)** for the Spanish translation
* **[rampo](https://github.com/rampo)** for the Italian translation
* **[erickpires](https://github.com/erickpires)** for the Brazilian Portuguese translation

License
-------

    Copyright 2014 SimplicityApks

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    See the LICENSE file for more information.
   
---
