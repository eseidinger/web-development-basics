import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiFetch } from '../../lib/api-client'
import type { BoardSummary, CreateBoardRequest } from './types'

export function useBoardsQuery(enabled: boolean) {
  return useQuery({
    queryKey: ['boards'],
    queryFn: () => apiFetch<BoardSummary[]>('/api/v1/boards'),
    enabled,
    staleTime: 60_000,
  })
}

export function useCreateBoardMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateBoardRequest) =>
      apiFetch<BoardSummary>('/api/v1/boards', {
        method: 'POST',
        body: JSON.stringify(request),
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['boards'] })
    },
  })
}
