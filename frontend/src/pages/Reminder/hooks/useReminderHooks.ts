import { useQueryClient } from '@tanstack/react-query';
import useChangeDate from 'hooks/queries/reminder/useChangeDate';
import useReminder from 'hooks/queries/reminder/useReminder';
import useWater from 'hooks/queries/reminder/useWater';
import useAddToast from 'hooks/useAddToast';
import { REMINDER_URL } from 'apis/reminder';
import { convertDateKorYear } from 'utils/date';

const useReminderHooks = () => {
  const queryClient = useQueryClient();
  const { data: reminderData } = useReminder();
  const addToast = useAddToast();

  const { mutate: water } = useWater<string>({
    successCallback: (_, { body: { waterDate } }) => {
      queryClient.invalidateQueries({ queryKey: [REMINDER_URL] });
      addToast('success', `${convertDateKorYear(waterDate).slice(5)}에 물주기 완료`);
    },
    errorCallback: (error) => {
      addToast('error', error.message);
    },
  });

  const { mutate: changeDate } = useChangeDate<string>({
    successCallback: (_, { body: { nextWaterDate } }) => {
      queryClient.invalidateQueries({ queryKey: [REMINDER_URL] });
      addToast('success', `${convertDateKorYear(nextWaterDate).slice(5)}로 물주기 날짜 변경`);
    },
    errorCallback: (error) => {
      addToast('error', error.message);
    },
  });

  return { reminderData, water, changeDate };
};

export default useReminderHooks;
