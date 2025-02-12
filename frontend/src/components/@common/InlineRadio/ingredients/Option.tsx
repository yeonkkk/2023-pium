import { useId } from 'react';
import { HiddenRadio, Label, Content } from './Option.style';
import { useRadioContext } from 'hooks/useRadioContext';

interface OptionProps {
  value: string;
}

const Option = (props: React.PropsWithChildren<OptionProps>) => {
  const { value, children } = props;
  const { value: contextValue, setValue, name } = useRadioContext();
  const id = useId();

  return (
    <Label htmlFor={id}>
      <HiddenRadio
        type="radio"
        id={id}
        name={name}
        value={value}
        checked={value === contextValue}
        onChange={() => setValue(value)}
      />
      <Content>{children ? children : value}</Content>
    </Label>
  );
};

export default Option;
