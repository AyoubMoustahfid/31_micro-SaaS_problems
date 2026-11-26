import apiClient from '../../utils/apiClient';

export const uploadProcurementDatasets = async (formData) => {
  const response = await apiClient.post('/uploads', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000,
  });
  return response.data;
};

export const resetProcurementUploads = async () => {
  await apiClient.delete('/challenges/MS_01/uploads');
};

export const runContractSplittingAnalysis = async (config) => {
  const response = await apiClient.post('/contract-splitting/config', null, {
    params: config,
  });
  return response.data;
};
