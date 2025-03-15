import http from "k6/http";
import { check, sleep } from "k6";
//Define test settings
export let options = {
  vus: 1,
  iterations: 10,
};
export default function () {
  const url = "http://localhost/test";
  const params = {
    headers: {
      Authorization: "Bearer 7kGIJWIiT0bFw1UtZxfwRil785JvX1HsxFnw5A4ooaA",
    },
  };
  let res = http.get(url, params);
  console.log(`Request ${__ITER + 1}: Status code = ${res.status}`);
  check(res, {
    "Rate limit works correctly": (r) => r.status == 200 || r.status == 429,
    "Success Response": (r) => r.status == 200,
    "Rate Limit Exceeded": (r) => r.status == 429,
  });
}
