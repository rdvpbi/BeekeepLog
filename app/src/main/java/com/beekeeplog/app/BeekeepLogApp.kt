package com.beekeeplog.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Application entry point — initialises Hilt dependency graph. */
@HiltAndroidApp
class BeekeepLogApp : Application()
