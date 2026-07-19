import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { useAuth } from '../auth/AuthProvider'
import { useCreateBoardMutation } from './api'

const boardSchema = z.object({
  name: z.string().trim().min(1, 'Board name is required').max(80, 'Keep the name under 80 characters'),
})

type BoardForm = z.infer<typeof boardSchema>

export function BoardCreatePanel() {
  const auth = useAuth()
  const createBoardMutation = useCreateBoardMutation()
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

  async function onSubmit(form: BoardForm) {
    await createBoardMutation.mutateAsync({ name: form.name })
    reset()
  }

  return (
    <section className="panel">
      <div className="panel__title-row">
        <div>
          <p className="panel__eyebrow">Forms</p>
          <h2>Create board</h2>
        </div>
        <span className="status-pill">Hook Form + Zod</span>
      </div>

      <form className="board-form" onSubmit={handleSubmit(onSubmit)}>
        <label htmlFor="board-name">Board name</label>
        <input
          id="board-name"
          type="text"
          placeholder="Migration Planning"
          disabled={!auth.isAuthenticated || createBoardMutation.isPending}
          {...register('name')}
        />
        {errors.name ? <p className="field-error">{errors.name.message}</p> : null}
        {createBoardMutation.isError ? <p className="field-error">{createBoardMutation.error.message}</p> : null}
        <button type="submit" disabled={!auth.isAuthenticated || createBoardMutation.isPending}>
          {createBoardMutation.isPending ? 'Creating…' : 'Create board'}
        </button>
      </form>

      {!auth.isAuthenticated ? (
        <p className="empty-state">Keycloak login is required before the form posts to the API.</p>
      ) : null}
    </section>
  )
}
