import { ComponentPropsWithoutRef } from 'react';
import theme from 'style/theme.style';

export const ICONS = [
  'account-circle',
  'arrow-drop-down',
  'arrow-left',
  'arrow-right',
  'arrow-right-alt',
  'calendar',
  'checkbox-empty',
  'checkbox-fill',
  'check-circle',
  'close-circle',
  'crown',
  'flowerpot',
  'fragrance',
  'house',
  'humidity',
  'info-circle',
  'line-arrow-left',
  'line-arrow-right',
  'manage-level-정보없음',
  'manage-level-초보자',
  'manage-level-경험자',
  'manage-level-전문가',
  'potted-plant',
  'search',
  'stopwatch',
  'sun',
  'thermometer-snow',
  'thermometer-sun',
  'tree-plant-pot',
  'warning',
  'water',
  'wind',
  'add-circle',
  'survey',
] as const;

type IconIds = (typeof ICONS)[number];

interface SvgIconsProps extends Omit<ComponentPropsWithoutRef<'svg'>, 'width' | 'height'> {
  icon: IconIds;
  size?: number;
  color?: string;
}

const SvgFill = ({ icon, size = 24, color = theme.color.gray, ...rest }: SvgIconsProps) => {
  return (
    <svg fill={color} width={size} height={size} {...rest}>
      <use href={`#${icon}`} />
    </svg>
  );
};

export default SvgFill;
