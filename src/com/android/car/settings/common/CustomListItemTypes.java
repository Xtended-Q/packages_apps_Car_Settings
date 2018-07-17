/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.car.settings.common;

/**
 * Keeps ViewType ids for all customer ListItems in a centralized location.
 */
public final class CustomListItemTypes {
    // According to ListItemAdapter, customized view type needs to be negative.
    public static final int CHECK_BOX_VIEW_TYPE = -1;
    public static final int EDIT_TEXT_VIEW_TYPE = -2;
    public static final int PASSWORD_VIEW_TYPE = -3;
    public static final int SPINNER_VIEW_TYPE = -4;
    public static final int SUGGESTION_VIEW_TYPE = -5;

    /**
     * No one should instantiate this class.
     */
    private CustomListItemTypes() {
    }
}