import { useState } from 'react'
import { closestCenter, DndContext, type DragEndEvent } from '@dnd-kit/core'
import {
  SortableContext,
  arrayMove,
  horizontalListSortingStrategy,
  useSortable,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'

type Column = {
  id: string
  name: string
  count: number
}

const initialColumns: Column[] = [
  { id: 'backlog', name: 'Backlog', count: 7 },
  { id: 'doing', name: 'Doing', count: 3 },
  { id: 'review', name: 'Review', count: 2 },
  { id: 'done', name: 'Done', count: 11 },
]

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

export function LaneBoardDemo() {
  const [columns, setColumns] = useState(initialColumns)

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

  return (
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
  )
}
