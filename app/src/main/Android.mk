#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_STATIC_ANDROID_LIBRARIES := \
    $(ANDROID_SUPPORT_DESIGN_TARGETS) \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview \
    android-support-v13 \
    android-support-annotations

LOCAL_STATIC_JAVA_AAR_LIBRARIES := material-dialogs-core-matlog-target \
    material-dialogs-commons-matlog-target \
    material-progressbar-library-matlog-target

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(foreach lib, $(LOCAL_STATIC_JAVA_AAR_LIBRARIES),\
  $(call intermediates-dir-for,JAVA_LIBRARIES,$(lib),,COMMON)/aar/res)

LOCAL_USE_AAPT2 := true

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.afollestad.materialdialogs \
    --extra-packages com.afollestad.materialdialogs.commons \
    --extra-packages me.zhanghai.android.materialprogressbar

LOCAL_JAR_EXCLUDE_FILES := none
LOCAL_PROGUARD_FLAG_FILES := proguard-rules.pro
LOCAL_SRC_FILES += $(call all-java-files-under, java)
LOCAL_PACKAGE_NAME := MatLog
LOCAL_SDK_VERSION := current
LOCAL_MODULE_TAGS := optional
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    material-dialogs-core-matlog:libs/aar/material-dialogs-core-0.9.4.2.aar \
    material-dialogs-commons-matlog:libs/aar/material-dialogs-commons-0.9.4.2.aar \
    material-progressbar-library-matlog:libs/aar/material-progressbar-library-1.3.0.aar

include $(BUILD_HOST_PREBUILT)

# Enumerate target prebuilts to avoid linker warnings
include $(CLEAR_VARS)

LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := material-dialogs-core-matlog-target
LOCAL_SRC_FILES := libs/aar/material-dialogs-core-0.9.4.2.aar
LOCAL_UNINSTALLABLE_MODULE := true

include $(BUILD_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := material-dialogs-commons-matlog-target
LOCAL_SRC_FILES := libs/aar/material-dialogs-commons-0.9.4.2.aar
LOCAL_UNINSTALLABLE_MODULE := true

include $(BUILD_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE_CLASS := JAVA_LIBRARIES
LOCAL_MODULE := material-progressbar-library-matlog-target
LOCAL_SRC_FILES := libs/aar/material-progressbar-library-1.3.0.aar
LOCAL_UNINSTALLABLE_MODULE := true

include $(BUILD_PREBUILT)
