export type BoardSummary = {
  id: string
  name: string
  role: 'owner' | 'member' | 'viewer'
  archivedAt: string | null
  createdAt: string
}

export type CreateBoardRequest = {
  name: string
}
