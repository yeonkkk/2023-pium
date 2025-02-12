import type { Meta, StoryObj } from '@storybook/react';
import { Suspense } from 'react';
import Loading from 'pages/Loading';
import NotFound from './NotFound';

const meta: Meta<typeof NotFound> = {
  component: NotFound,
  parameters: {
    reactRouter: {
      routePath: '/dict/:id',
      routeParams: { id: '1' },
    },
  },
  decorators: [
    (Story) => {
      return (
        <Suspense fallback={<Loading />}>
          <Story />
        </Suspense>
      );
    },
  ],
};

export default meta;

type Story = StoryObj<typeof NotFound>;

export const Default: Story = {};
