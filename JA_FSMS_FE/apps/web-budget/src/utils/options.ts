import dayjs from 'dayjs';

import { $t } from '#/locales';
// 通过year正数表示未来几年，负数表示过去几年
export const yearOptions = (
  year: number = -10,
  showSuffix: boolean = true,
): { label: string; value: string }[] => {
  const years: string[] = [];
  const currentYear = dayjs().year();
  if (year > 0) {
    // 未来n年（包含当前年）
    for (let i = 0; i < year; i++) {
      years.push(String(currentYear + i));
    }
  } else if (year < 0) {
    // 过去n年（包含当前年），如-5 表示[今年, 去年, ...]
    for (let i = 0; i < Math.abs(year); i++) {
      years.push(String(currentYear - i));
    }
  } else {
    years.push(String(currentYear));
  }
  return years.map((yr) => ({
    label: `${yr}${showSuffix ? $t('common.year') : ''}`,
    value: yr,
  }));
};

export const quarterOptions = (
  quarter: number = 4,
): { label: string; value: number | string }[] => {
  const quarters = [];
  for (let i = 1; i <= quarter; i++) {
    quarters.push({
      label: `Q${i}`,
      value: i,
    });
  }
  return quarters;
};

export const monthOptions = (
  month: number = 12,
): { label: string; value: number | string }[] => {
  const months = [];
  for (let i = 1; i <= month; i++) {
    months.push({
      label: `${i}${$t('common.month')}`,
      value: i,
    });
  }
  return months;
};
