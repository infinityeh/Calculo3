"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import { useSession, signIn } from "next-auth/react";
import Link from "next/link";

type Message = {
  id: string;
  role: "user" | "assistant" | "system";
  text: string;
  sessionId?: string;
};

type ChatResponse = {
  sessionId: string;
  assistantMessageId: string;
  text: string;
  blocked?: boolean;
  system?: string;
};

export default function ChatPage() {
  const { data: session, status } = useSession();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [sessionId, setSessionId] = useState<string | undefined>();
  const [assignmentTag, setAssignmentTag] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [consentVisible, setConsentVisible] = useState(false);
  const [consentAccepted, setConsentAccepted] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  useEffect(() => {
    if (status === "authenticated" && !consentAccepted) {
      const stored = localStorage.getItem("prompt-lab-consent");
      if (!stored) {
        setConsentVisible(true);
      } else {
        setConsentAccepted(true);
      }
    }
  }, [status, consentAccepted]);

  const sendConsent = async () => {
    await fetch("/api/consent-proxy", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ version: "v1", accepted: true })
    });
    localStorage.setItem("prompt-lab-consent", "v1");
    setConsentAccepted(true);
    setConsentVisible(false);
  };

  const submitPrompt = async (event: FormEvent) => {
    event.preventDefault();
    if (!input.trim()) return;
    if (!session) {
      signIn("google");
      return;
    }
    setLoading(true);
    const userMessage: Message = { id: crypto.randomUUID(), role: "user", text: input };
    setMessages((prev) => [...prev, userMessage]);
    const response = await fetch("/api/chat-proxy", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ prompt: input, sessionId, assignmentTag: assignmentTag || null })
    });
    const data: ChatResponse = await response.json();
    if (data.blocked) {
      setMessages((prev) => [
        ...prev,
        { id: crypto.randomUUID(), role: "system", text: data.system ?? "Prompt bloqueado" }
      ]);
    } else {
      setSessionId(data.sessionId);
      setMessages((prev) => [
        ...prev,
        { id: data.assistantMessageId, role: "assistant", text: data.text, sessionId: data.sessionId }
      ]);
    }
    setInput("");
    setLoading(false);
  };

  const sendFeedback = async (messageId: string, thumbsUp: boolean, usefulness: number) => {
    await fetch("/api/feedback-proxy", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ messageId, thumbsUp, usefulness })
    });
  };

  if (status === "loading") {
    return <p className="p-8">Carregando...</p>;
  }

  if (!session) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-8">
        <p>É necessário autenticar para acessar o laboratório.</p>
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
    <main className="mx-auto flex min-h-screen max-w-4xl flex-col gap-4 p-4">
      <header className="flex items-center justify-between rounded-lg bg-white p-4 shadow">
        <div>
          <h1 className="text-2xl font-semibold">Laboratório de Prompts</h1>
          <p className="text-sm text-slate-500">Sessão: {sessionId ?? "nova"}</p>
        </div>
        <Link className="text-sm text-indigo-600 underline" href="/">
          Voltar
        </Link>
      </header>

      <section className="rounded-lg bg-white p-4 shadow">
        <form className="flex flex-col gap-3" onSubmit={submitPrompt}>
          <label className="text-sm font-medium text-slate-600" htmlFor="assignment">
            Tag da atividade (opcional)
          </label>
          <input
            id="assignment"
            value={assignmentTag}
            onChange={(event) => setAssignmentTag(event.target.value)}
            className="w-full rounded border border-slate-300 p-2"
            placeholder="Ex.: aula-5"
          />
          <label className="text-sm font-medium text-slate-600" htmlFor="prompt">
            Prompt
          </label>
          <textarea
            id="prompt"
            value={input}
            onChange={(event) => setInput(event.target.value)}
            className="h-32 w-full rounded border border-slate-300 p-2"
            placeholder="Descreva sua dúvida envolvendo Cálculo 3"
          />
          <button
            type="submit"
            disabled={loading}
            className="self-end rounded bg-indigo-600 px-4 py-2 text-white shadow hover:bg-indigo-500 disabled:opacity-50"
          >
            {loading ? "Enviando..." : "Enviar"}
          </button>
        </form>
      </section>

      <section className="flex-1 rounded-lg bg-white p-4 shadow">
        <div ref={scrollRef} className="flex h-96 flex-col gap-4 overflow-y-auto">
          {messages.map((message) => (
            <div key={message.id} className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`}>
              <div
                className={`max-w-xl rounded-lg p-3 shadow ${
                  message.role === "user"
                    ? "bg-indigo-600 text-white"
                    : message.role === "assistant"
                      ? "bg-slate-100"
                      : "bg-yellow-100"
                }`}
              >
                <p className="whitespace-pre-wrap text-sm">{message.text}</p>
                {message.role === "assistant" && (
                  <div className="mt-2 flex items-center gap-2 text-xs text-slate-500">
                    <span>Avalie:</span>
                    <button
                      onClick={() => sendFeedback(message.id, true, 5)}
                      className="rounded bg-green-100 px-2 py-1"
                      type="button"
                    >
                      👍
                    </button>
                    <button
                      onClick={() => sendFeedback(message.id, false, 0)}
                      className="rounded bg-red-100 px-2 py-1"
                      type="button"
                    >
                      👎
                    </button>
                    <div className="flex items-center gap-1">
                      {[0, 1, 2, 3, 4, 5].map((score) => (
                        <button
                          key={score}
                          type="button"
                          className="rounded bg-slate-200 px-1"
                          onClick={() => sendFeedback(message.id, score >= 3, score)}
                        >
                          {score}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          ))}
          {messages.length === 0 && (
            <p className="text-center text-sm text-slate-400">
              Envie um prompt para iniciar a conversa com o assistente Gemini.
            </p>
          )}
        </div>
      </section>

      {consentVisible && (
        <div className="fixed inset-0 flex items-center justify-center bg-black/40">
          <div className="max-w-lg rounded-lg bg-white p-6 shadow-lg">
            <h2 className="text-xl font-semibold">Termo de consentimento</h2>
            <p className="mt-3 text-sm text-slate-600">
              Este laboratório faz parte de um estudo acadêmico. Registramos apenas seu e-mail institucional e as
              mensagens enviadas. Ao continuar você concorda com o uso dos dados para fins de pesquisa.
            </p>
            <div className="mt-6 flex justify-end gap-3">
              <button
                onClick={() => setConsentVisible(false)}
                className="rounded border border-slate-300 px-4 py-2"
              >
                Cancelar
              </button>
              <button onClick={sendConsent} className="rounded bg-indigo-600 px-4 py-2 text-white">
                Aceitar e continuar
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
