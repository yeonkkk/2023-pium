import type { EditPetPlantRequest, PetPlantDetails } from 'types/petPlant';
import { useMutation } from '@tanstack/react-query';
import { generatePath, useNavigate } from 'react-router-dom';
import useAddToast from 'hooks/useAddToast';
import useUnauthorize from 'hooks/useUnauthorize';
import PetAPI from 'apis/pet';
import { throwOnInvalidStatus } from 'utils/throwOnInvalidStatus';
import { URL_PATH } from 'constants/index';

const useEditPetPlant = (petPlantId: PetPlantDetails['id']) => {
  const navigate = useNavigate();
  const { retryCallback } = useUnauthorize();
  const addToast = useAddToast();

  return useMutation<void, Error, EditPetPlantRequest>({
    mutationFn: async (form) => {
      const response = await PetAPI.edit(petPlantId, form);
      throwOnInvalidStatus(response);
    },

    onSuccess: () => {
      addToast('success', '반려 식물 정보를 바꿨습니다.');
      navigate(generatePath(URL_PATH.petDetail, { id: petPlantId.toString() }), { replace: true });
    },

    onError: () => {
      addToast('error', '반려 식물 정보 수정에 실패했어요.');
    },
    throwOnError: true,
    retry: retryCallback,
  });
};

export default useEditPetPlant;
