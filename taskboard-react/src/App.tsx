import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { closestCenter, DndContext, type DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  horizontalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import './App.css'

type BoardSummary = {
  id: string
  name: string
  role: 'owner' | 'member' | 'viewer'
  archivedAt: string | null
  createdAt: string
}

type Column = {
  id: string
  name: string
  count: number
}

const boardSchema = z.object({
  name: z.string().trim().min(1, 'Board name is required').max(80, 'Keep the name under 80 characters'),
})

type BoardForm = z.infer<typeof boardSchema>

const initialColumns: Column[] = [
  { id: 'backlog', name: 'Backlog', count: 7 },
  { id: 'doing', name: 'Doing', count: 3 },
  { id: 'review', name: 'Review', count: 2 },
  { id: 'done', name: 'Done', count: 11 },
]

async function loadBoards(): Promise<BoardSummary[]> {
  const apiBase = import.meta.env.VITE_TASKBOARD_API_URL?.trim()
  if (!apiBase) {
    return []
  }

  const response = await fetch(`${apiBase}/api/v1/boards`, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error(`Board request failed with status ${response.status}`)
  }

  return (await response.json()) as BoardSummary[]
}

function SortableLane({ column }: { column: Column }) {
  const { attributes, listeners, setNodeRef, transform, transition } = useSortable({ id: column.id })

  return (
    <article
      ref={setNodeRef}
      className="lane"
      style={{
        transform: CSS.Transform.toString(transform),
        transition,
      }}
      {...attributes}
      {...listeners}
    >
      <div className="lane__header">
        <h3>{column.name}</h3>
        <span>{column.count}</span>
      </div>
      <p>Drag to compare how much orchestration React needs for sortable workflow columns.</p>
    </article>
  )
}

function App() {
  const [columns, setColumns] = useState(initialColumns)
  const [draftBoards, setDraftBoards] = useState<string[]>([])

  const boardQuery = useQuery({
    queryKey: ['boards'],
    queryFn: loadBoards,
    staleTime: 60_000,
  })

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<BoardForm>({
    resolver: zodResolver(boardSchema),
    defaultValues: {
      name: '',
    },
  })

  function onDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (!over || active.id === over.id) {
      return
    }

    setColumns((currentColumns) => {
      const oldIndex = currentColumns.findIndex((column) => column.id === active.id)
      const newIndex = currentColumns.findIndex((column) => column.id === over.id)
      return arrayMove(currentColumns, oldIndex, newIndex)
    })
  }

  function onSubmit(form: BoardForm) {
    setDraftBoards((currentBoards) => [form.name, ...currentBoards])
    reset()
  }

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <p className="eyebrow">React comparison baseline</p>
        <h1>Task Board with the lean stack only.</h1>
        <p className="hero-copy">
          This setup uses React Router, TanStack Query, React Hook Form, Zod, and dnd-kit.
          No global state layer, no UI kit, no extra component abstractions.
        </p>
      </section>

      <section className="content-grid">
        <section className="panel">
          <div className="panel__title-row">
            <div>
              <p className="panel__eyebrow">Server state</p>
              <h2>Boards query</h2>
            </div>
            <span className="status-pill">TanStack Query</span>
          </div>

          {!import.meta.env.VITE_TASKBOARD_API_URL ? (
            <p className="empty-state">
              Set <code>VITE_TASKBOARD_API_URL</code> to point at the backend and this panel will fetch boards.
            </p>
          ) : null}

          {boardQuery.isLoading ? <p className="empty-state">Loading boards…</p> : null}
          {boardQuery.isError ? (
            <p className="empty-state error">{(boardQuery.error as Error).message}</p>
          ) : null}
          {boardQuery.data && boardQuery.data.length > 0 ? (
            <ul className="board-list">
              {boardQuery.data.map((board) => (
                <li key={board.id}>
                  <span>{board.name}</span>
                  <small>{board.role}</small>
                </li>
              ))}
            </ul>
          ) : null}
        </section>

        <section className="panel">
          <div className="panel__title-row">
            <div>
              <p className="panel__eyebrow">Forms</p>
              <h2>Create board draft</h2>
            </div>
            <span className="status-pill">Hook Form + Zod</span>
          </div>

          <form className="board-form" onSubmit={handleSubmit(onSubmit)}>
            <label htmlFor="board-name">Board name</label>
            <input id="board-name" type="text" placeholder="Migration Planning" {...register('name')} />
            {errors.name ? <p className="field-error">{errors.name.message}</p> : null}
            <button type="submit">Add local draft</button>
          </form>

          {draftBoards.length > 0 ? (
            <ul className="draft-list">
              {draftBoards.map((boardName) => (
                <li key={boardName}>{boardName}</li>
              ))}
            </ul>
          ) : (
            <p className="empty-state">Draft boards appear here after schema-validated submission.</p>
          )}
        </section>
      </section>

      <section className="panel panel--wide">
        <div className="panel__title-row">
          <div>
            <p className="panel__eyebrow">Interaction</p>
            <h2>Sortable workflow columns</h2>
          </div>
          <span className="status-pill">dnd-kit</span>
        </div>

        <DndContext collisionDetection={closestCenter} onDragEnd={onDragEnd}>
          <SortableContext items={columns.map((column) => column.id)} strategy={horizontalListSortingStrategy}>
            <div className="lane-grid">
              {columns.map((column) => (
                <SortableLane key={column.id} column={column} />
              ))}
            </div>
          </SortableContext>
        </DndContext>
      </section>
    </main>
  )
}

export default App
