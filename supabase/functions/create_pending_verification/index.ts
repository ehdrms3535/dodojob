import { serve } from "https://deno.land/std/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};

function randomToken(len = 40) {
  const arr = crypto.getRandomValues(new Uint8Array(len));
  return Array.from(arr, b => b.toString(16).padStart(2, "0")).join("");
}

serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: cors });

  try {
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL")!,          // 플랫폼이 자동 주입
      Deno.env.get("SERVICE_ROLE_KEY")!,      // ← 우리가 방금 저장한 이름
    );

    const body = await req.json().catch(() => ({}));
    const { provider, tx_id, name, phone, birth_date, gender, region } = body ?? {};
    if (!provider || !tx_id) {
      return new Response(JSON.stringify({ error: "missing provider/tx_id" }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }

    const token = randomToken(32);
    const { error } = await supabase
      .from("pending_verifications")
      .insert({ token, provider, tx_id, name, phone, birth_date, gender, region });

    if (error) {
      return new Response(JSON.stringify({ error: error.message }), {
        status: 400, headers: { ...cors, "Content-Type": "application/json" },
      });
    }
    return new Response(JSON.stringify({ token }), {
      headers: { ...cors, "Content-Type": "application/json" },
    });
  } catch (e) {
    return new Response(JSON.stringify({ error: String(e) }), {
      status: 500, headers: { ...cors, "Content-Type": "application/json" },
    });
  }
});
