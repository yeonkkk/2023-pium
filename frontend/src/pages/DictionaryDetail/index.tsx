import { useParams } from 'react-router-dom';
import Image from 'components/@common/Image';
import Navbar from 'components/@common/Navbar';
import DictionaryPlantContent from 'components/dictionaryPlant/DictionaryPlantContent';
import { Main } from './DictionaryDetail.style';
import useDictDetail from 'hooks/queries/dictionary/useDictDetail';

const DictionaryDetail = () => {
  const { id } = useParams();
  if (!id) throw new Error('URL에 id가 없습니다.');

  const dictionaryPlantId = Number(id);
  const { data: dictionaryPlantDetail, isSuccess } = useDictDetail(dictionaryPlantId);
  if (!isSuccess) return null;

  const { image, name } = dictionaryPlantDetail;

  return (
    <>
      <Main>
        <Image type="wide" src={image} alt={name} size="300px" />
        <DictionaryPlantContent {...dictionaryPlantDetail} />
      </Main>
      <Navbar />
    </>
  );
};

export default DictionaryDetail;
