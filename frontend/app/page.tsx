"use client";

import { signIn, signOut, useSession } from "next-auth/react";
import Link from "next/link";

export default function HomePage() {
  const { data: session, status } = useSession();

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-6 p-4">
      <div className="max-w-xl rounded-xl bg-white p-8 shadow">
        <h1 className="text-3xl font-bold text-slate-800">Prompt-Lab Engenharia de Prompt</h1>
        <p className="mt-4 text-slate-600">
          Plataforma experimental para estudar interações de estudantes de Cálculo 3 com modelos generativos.
        </p>
        {status === "loading" && <p className="mt-4">Carregando sessão...</p>}
        {session ? (
          <div className="mt-6 flex flex-col gap-4">
            <p className="text-sm text-slate-500">Autenticado como {session.user?.email}</p>
            <div className="flex gap-3">
              <Link
                href="/chat"
                className="rounded bg-indigo-600 px-4 py-2 text-white shadow hover:bg-indigo-500"
              >
                Abrir laboratório
              </Link>
              <button
                onClick={() => signOut()}
                className="rounded border border-slate-300 px-4 py-2 text-slate-700 hover:bg-slate-100"
              >
                Sair
              </button>
            </div>
          </div>
        ) : (
          <button
            onClick={() => signIn("google")}
            className="mt-6 rounded bg-green-600 px-4 py-2 text-white shadow hover:bg-green-500"
          >
            Entrar com Google
          </button>
        )}
      </div>
      <Link className="text-sm text-slate-500 underline" href="/admin">
        Área administrativa
      </Link>
    </main>
  );
}
