import type { Meta, StoryObj } from '@storybook/react';
import { useState } from 'react';
import Stack from '.';
import useStackContext from './hooks/useStackContext';

const meta: Meta<typeof Stack> = {
  component: Stack,
};

export default meta;

type Story = StoryObj<typeof Stack>;

const TestButton = ({ type }: { type: 'big' | 'small' }) => {
  const { showNextElement, isLastElementShown } = useStackContext();

  return (
    <button
      type="button"
      onClick={() => showNextElement()}
      style={{
        display: 'block',
        width: '100%',
        height: `${type === 'big' ? '100px' : '30px'}`,
        border: `2px solid ${isLastElementShown ? 'black' : 'lightpink'}`,
      }}
    >
      {type === 'big' ? '큼직한 버튼' : '조막만한 버튼'}
    </button>
  );
};

/**
 * Stack 안쪽 컴포넌트에서 useStackContext의 showNextElment를 호출하여 다음 스택 요소를 보여줍니다.
 *
 * 분홍색 버튼은 스토리북 확인을 위한 임시 컴포넌트이며, 실제 Stack에는 스타일이 존재하지 않습니다.
 */

export const PlainExample: Story = {
  render: () => (
    <Stack>
      <Stack.Element height="30px">
        <TestButton type="small" />
      </Stack.Element>
      <Stack.Element height="100px">
        <TestButton type="big" />
      </Stack.Element>
      <Stack.Element height="30px">
        <TestButton type="small" />
      </Stack.Element>
      <Stack.Element height="30px">
        <TestButton type="small" />
      </Stack.Element>
      <Stack.Element height="100px">
        <TestButton type="big" />
      </Stack.Element>
      <Stack.Element height="30px">
        <TestButton type="small" />
      </Stack.Element>
    </Stack>
  ),
};

const Counter = () => {
  const [count, setCount] = useState(0);
  const { showNextElement } = useStackContext();

  return (
    <div
      style={{
        border: '2px solid lightpink',
        height: '100px',
        padding: '20px',
      }}
    >
      <p style={{ fontSize: '2rem' }}>{count}원 모았습니다.</p>
      <div>
        <button type="button" onClick={() => setCount(count + 1)}>
          돈 벌기
        </button>
      </div>
      <div>
        <button type="button" onClick={() => showNextElement()}>
          다음 통장 보여주기
        </button>
      </div>
    </div>
  );
};

/**
 * 역순으로 표시하지만 실제 DOM 순서를 꼬는 것이 아니기 때문에 버튼을 눌러 상태를 변경하더라도 상태는 변하지 않습니다.
 */

export const StateExample: Story = {
  render: () => (
    <Stack>
      <Stack.Element height="100px">
        <Counter />
      </Stack.Element>
      <Stack.Element height="100px">
        <Counter />
      </Stack.Element>
      <Stack.Element height="100px">
        <Counter />
      </Stack.Element>
    </Stack>
  ),
};