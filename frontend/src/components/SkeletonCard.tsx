export default function SkeletonCard() {
  return (
    <div className="card overflow-hidden animate-pulse border-ink/5">
      <div className="aspect-[2/3] bg-paper-dark" />
      <div className="p-4 space-y-3 border-t border-ink/5">
        <div className="h-3 bg-paper-dark rounded w-1/4" />
        <div className="h-5 bg-paper-dark rounded w-5/6" />
        <div className="h-3 bg-paper-dark rounded w-2/3" />
        <div className="h-6 bg-paper-dark rounded w-1/3 mt-4" />
        <div className="h-10 bg-paper-dark rounded mt-3" />
      </div>
    </div>
  )
}
