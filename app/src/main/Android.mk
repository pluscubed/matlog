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

ifeq ($(TARGET_BUILD_APPS),)
support_library_root_dir := frameworks/support
else
support_library_root_dir := prebuilts/sdk/current/support
endif

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview \
    android-support-annotations \
    android-support-design

LOCAL_STATIC_JAVA_AAR_LIBRARIES += material-dialogs-core-matlog \
    material-dialogs-commons-matlog \
    material-progressbar-library-matlog

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
                      $(support_library_root_dir)/v7/appcompat/res \
                      $(support_library_root_dir)/v7/recyclerview/res \
                      $(support_library_root_dir)/design/res

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.appcompat \
    --extra-packages android.support.v7.recyclerview \
    --extra-packages android.support.design \
    --extra-packages com.afollestad.materialdialogs \
    --extra-packages com.afollestad.materialdialogs.commons \
    --extra-packages me.zhanghai.android.materialprogressbar

LOCAL_JAR_EXCLUDE_FILES := none
LOCAL_PROGUARD_FLAG_FILES := proguard-rules.pro
LOCAL_SRC_FILES += $(call all-java-files-under, java)
LOCAL_PACKAGE_NAME := MatLog
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := optional
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    material-dialogs-core-matlog:libs/aar/material-dialogs-core-0.9.4.2.aar \
    material-dialogs-commons-matlog:libs/aar/material-dialogs-commons-0.9.4.2.aar \
    material-progressbar-library-matlog:libs/aar/material-progressbar-library-1.3.0.aar

include $(BUILD_MULTI_PREBUILT)
