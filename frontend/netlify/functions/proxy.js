import OpenAI from "openai";


export async function handler(event) {
    try {
        const body = JSON.parse(event.body);
        const message = await sendMessage(`${process.env.OPENAI_API_KEY}`, body);
        return {
            statusCode: 200,
            body: JSON.stringify(message),
        };
    } catch (err) {
        return {
            statusCode: 500,
            body: JSON.stringify(err)
        }
    }
}

// TODO: check if the convo contains less tokens than the models context window size
async function sendMessage(key, messages) {
    const client = new OpenAI({ apiKey: key });
    const completion = await client.chat.completions.create({
        model: "gpt-4o-mini",
        messages: messages,
        max_tokens: 200
    });
    const response = completion.choices[0].message.content;
    return response;
}
