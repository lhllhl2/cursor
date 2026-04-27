<script setup lang="ts">
import { FORM_STYLE, SUPPORT_LANGUAGES } from '@vben/constants';
import { $t } from '@vben/locales';

import NumberFieldItem from '../number-field-item.vue';
import SelectItem from '../select-item.vue';
import SwitchItem from '../switch-item.vue';

defineOptions({
  name: 'PreferenceGeneralConfig',
});

const appLocale = defineModel<string>('appLocale');
const appDynamicTitle = defineModel<boolean>('appDynamicTitle');
const appWatermark = defineModel<boolean>('appWatermark');
const appEnableCheckUpdates = defineModel<boolean>('appEnableCheckUpdates');
const appZoom = defineModel<number>('appZoom');
const appFormStyle = defineModel<string>('appFormStyle');
</script>

<template>
  <NumberFieldItem v-model="appZoom" :min="20" :step="5">
    {{ $t('preferences.zoom') }}
  </NumberFieldItem>
  <SelectItem v-model="appFormStyle" :items="FORM_STYLE">
    {{ $t('preferences.formStyle') }}
  </SelectItem>
  <SelectItem v-model="appLocale" :items="SUPPORT_LANGUAGES">
    {{ $t('preferences.language') }}
  </SelectItem>
  <SwitchItem v-model="appDynamicTitle">
    {{ $t('preferences.dynamicTitle') }}
  </SwitchItem>
  <SwitchItem v-model="appWatermark">
    {{ $t('preferences.watermark') }}
  </SwitchItem>
  <SwitchItem v-model="appEnableCheckUpdates">
    {{ $t('preferences.checkUpdates') }}
  </SwitchItem>
</template>
