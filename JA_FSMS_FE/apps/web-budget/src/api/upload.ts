import { requestClient } from '#/api/request';

export interface UploadFileParams {
  url: string;
  file: File;
  onError?: (error: Error) => void;
  onProgress?: (progress: { percent: number }) => void;
  onSuccess?: (data: any, file: File) => void;
}
export async function uploadFile({
  url,
  file,
  // onError,
  onProgress,
  onSuccess,
}: UploadFileParams) {
  // try {
  onProgress?.({ percent: 0 });

  const data = await requestClient.upload(url, { file });

  onProgress?.({ percent: 100 });
  onSuccess?.(data, file);
  // } catch (error) {
  //   onError?.(error instanceof Error ? error : new Error(String(error)));
  // }
}
