export default function SkeletonCard() {
  return (
    <div className="card overflow-hidden animate-pulse border-stone-100">
      <div className="aspect-[4/3] bg-gradient-to-br from-stone-200 to-stone-100" />
      <div className="p-5 space-y-3 border-t border-stone-100">
        <div className="h-3 bg-stone-200 rounded-full w-1/4" />
        <div className="h-5 bg-stone-200 rounded-lg w-5/6" />
        <div className="h-3 bg-stone-200 rounded-lg w-2/3" />
        <div className="h-8 bg-stone-200 rounded-lg w-1/3 mt-4" />
      </div>
    </div>
  )
}
