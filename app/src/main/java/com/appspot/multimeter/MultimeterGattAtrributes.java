/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.appspot.multimeter;

import java.util.HashMap;

/**
 * This class includes the standard GATT attributes of the Multimeter profile.
 */
public class MultimeterGattAtrributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static String MULTIMETER_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String MULTIMETER_MEASUREMENT = "0000fff4-0000-1000-8000-00805f9b34fb";
    public static String MULTIMETER_MODE = "0000fff1-0000-1000-8000-00805f9b34fb";

}
