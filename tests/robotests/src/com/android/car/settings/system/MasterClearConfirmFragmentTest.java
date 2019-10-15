/*
 * Copyright 2019 The Android Open Source Project
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

package com.android.car.settings.system;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.service.oemlock.OemLockManager;
import android.service.persistentdata.PersistentDataBlockManager;

import androidx.preference.PreferenceManager;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.R;
import com.android.car.settings.testutils.FragmentController;
import com.android.car.settings.testutils.ShadowOemLockManager;
import com.android.car.settings.testutils.ShadowPersistentDataBlockManager;
import com.android.car.ui.toolbar.MenuItem;
import com.android.car.ui.toolbar.Toolbar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.util.ReflectionHelpers;

import java.util.Map;

/** Unit test for {@link MasterClearConfirmFragment}. */
@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowPersistentDataBlockManager.class, ShadowOemLockManager.class})
public class MasterClearConfirmFragmentTest {

    private Context mContext;
    private MasterClearConfirmFragment mFragment;

    @Before
    public void setUp() {
        // Robolectric doesn't know about the pdb manager, so we must add it ourselves.
        getSystemServiceMap().put(Context.PERSISTENT_DATA_BLOCK_SERVICE,
                PersistentDataBlockManager.class.getName());
        // Robolectric doesn't know about the oem lock manager, so we must add it ourselves.
        getSystemServiceMap().put(Context.OEM_LOCK_SERVICE, OemLockManager.class.getName());

        mContext = RuntimeEnvironment.application;
        mFragment = FragmentController.of(new MasterClearConfirmFragment()).setup();

        // Default to not provisioned.
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED,
                0);
    }

    @After
    public void tearDown() {
        getSystemServiceMap().remove(Context.PERSISTENT_DATA_BLOCK_SERVICE);
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED,
                0);
    }

    @Test
    public void confirmClicked_sendsResetIntent() {
        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getAction()).isEqualTo(Intent.ACTION_FACTORY_RESET);
        assertThat(resetIntent.getPackage()).isEqualTo("android");
        assertThat(resetIntent.getFlags() & Intent.FLAG_RECEIVER_FOREGROUND).isEqualTo(
                Intent.FLAG_RECEIVER_FOREGROUND);
        assertThat(resetIntent.getExtras().getString(Intent.EXTRA_REASON)).isEqualTo(
                "MasterClearConfirm");
    }

    @Test
    public void confirmClicked_resetEsimFalse_resetIntentReflectsChoice() {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean(
                mContext.getString(R.string.pk_master_clear_reset_esim), false).commit();

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getExtras().getBoolean(Intent.EXTRA_WIPE_ESIMS)).isEqualTo(false);
    }

    @Test
    public void confirmClicked_pdbManagerNull_sendsResetIntent() {
        getSystemServiceMap().remove(Context.PERSISTENT_DATA_BLOCK_SERVICE);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getAction()).isEqualTo(Intent.ACTION_FACTORY_RESET);
    }

    @Test
    public void confirmClicked_oemUnlockAllowed_doesNotWipePdb() {
        getShadowOemLockManager().setIsOemUnlockAllowed(true);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        assertThat(getShadowPdbManager().getWipeCalledCount()).isEqualTo(0);
    }

    @Test
    public void confirmClicked_oemUnlockAllowed_sendsResetIntent() {
        getShadowOemLockManager().setIsOemUnlockAllowed(true);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getAction()).isEqualTo(Intent.ACTION_FACTORY_RESET);
    }

    @Test
    public void confirmClicked_noOemUnlockAllowed_notProvisioned_doesNotWipePdb() {
        getShadowOemLockManager().setIsOemUnlockAllowed(false);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        assertThat(getShadowPdbManager().getWipeCalledCount()).isEqualTo(0);
    }

    @Test
    public void confirmClicked_noOemUnlockAllowed_notProvisioned_sendsResetIntent() {
        getShadowOemLockManager().setIsOemUnlockAllowed(false);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getAction()).isEqualTo(Intent.ACTION_FACTORY_RESET);
    }

    @Test
    public void confirmClicked_noOemUnlockAllowed_provisioned_wipesPdb() {
        getShadowOemLockManager().setIsOemUnlockAllowed(false);
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED,
                1);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        assertThat(getShadowPdbManager().getWipeCalledCount()).isEqualTo(1);
    }

    @Test
    public void confirmClicked_noOemUnlockAllowed_provisioned_sendsResetIntent() {
        getShadowOemLockManager().setIsOemUnlockAllowed(false);
        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED,
                1);

        findMasterClearConfirmButton(mFragment.requireActivity()).performClick();

        Intent resetIntent = ShadowApplication.getInstance().getBroadcastIntents().get(0);
        assertThat(resetIntent.getAction()).isEqualTo(Intent.ACTION_FACTORY_RESET);
    }

    private MenuItem findMasterClearConfirmButton(Activity activity) {
        Toolbar toolbar = activity.requireViewById(R.id.toolbar);
        return toolbar.getMenuItems().get(0);
    }

    private ShadowPersistentDataBlockManager getShadowPdbManager() {
        return Shadow.extract(mContext.getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE));
    }

    private ShadowOemLockManager getShadowOemLockManager() {
        return Shadow.extract(mContext.getSystemService(Context.OEM_LOCK_SERVICE));
    }

    private Map<String, String> getSystemServiceMap() {
        return ReflectionHelpers.getStaticField(ShadowContextImpl.class, "SYSTEM_SERVICE_MAP");
    }
}
