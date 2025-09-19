"use client";

import { useSession, signIn } from "next-auth/react";

export default function AdminPage() {
  const { data: session, status } = useSession();
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL ?? process.env.BACKEND_URL ?? "http://localhost:8080";

  if (status === "loading") {
    return <p className="p-8">Carregando...</p>;
  }

  if (!session) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-8">
        <p>Faça login com uma conta autorizada para exportar dados.</p>
        <button
          onClick={() => signIn("google")}
          className="rounded bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500"
        >
          Entrar com Google
        </button>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-8">
      <h1 className="text-2xl font-semibold">Exportar dados</h1>
      <p className="text-sm text-slate-600">
        Use o botão abaixo para baixar o CSV diretamente do backend (acesso restrito a administradores).
      </p>
      <a
        href={`${backendUrl}/api/export`}
        target="_blank"
        rel="noreferrer"
        className="rounded bg-green-600 px-4 py-2 text-white hover:bg-green-500"
      >
        Baixar CSV
      </a>
    </main>
  );
}
