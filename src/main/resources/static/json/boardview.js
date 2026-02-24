/* -------------------------------------------------------
 * 🔥 전역 함수 선언 (반드시 바깥에 있어야 함!)
 * ------------------------------------------------------- */
function showReplyForm(parentidx, parentUserId) { }
function deleteComent(idx) { }
function showEditForm(idx, ment) { }
function updateComent(idx) { }
/* -------------------------------------------------------
 * 메인 스크립트
 * ------------------------------------------------------- */
document.addEventListener("DOMContentLoaded", () => {

    const container = document.getElementById('comment-list-container');
    const form = document.getElementById('comment-post-form');
    let currentparentidx = null;

    /* ============================
     * 🔥 일반 댓글 등록 처리
     * ============================ */
    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const fd = new FormData(form);

        const dto = {
            boardIdx: parseInt(fd.get("boardIdx")),
            userId: fd.get("userId"),
            ment: fd.get("ment"),
            parentidx: null
        };

        fetch(COMMENT_API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        })
            .then(res => {
                if (checkAndHandleAuth(res)) return;
                return handleHtmlResponse(res);
            })
            .then(() => {
                form.reset();      // 입력창 비우기
                fetchComments();   // 댓글 다시 불러오기
            });
    });

    /* ============================
     * 공통: 인증 처리
     * ============================ */
    function checkAndHandleAuth(response) {
        if (response.status === 401 || response.status === 403) {
            alert('세션이 만료되었거나 권한이 없습니다.');
            window.location.href = '/login';
            return true;
        }
        return false;
    }

    /* ============================
     * HTML 응답 방지
     * ============================ */
    function handleHtmlResponse(response) {
        return response.text().then(text => {
            if (text.startsWith('<!DOCTYPE') || text.startsWith('<html')) {
                alert('세션이 만료 또는 권한 없음');
                window.location.href = '/login';
                return undefined;
            }
            return text;
        });
    }

    /* ============================
     * 🔥 대댓글 입력창 표시
     * ============================ */
    function _showReplyForm(parentidx, parentUserId) {

        const existing = document.querySelector('.reply-form-container');
        if (existing) existing.remove();

        if (parentidx === null || currentparentidx === parentidx) {
            currentparentidx = null;
            return;
        }

        currentparentidx = parentidx;

        const parentDiv = document.querySelector(`#comment-${parentidx}`);
        if (!parentDiv) return;

        parentDiv.insertAdjacentHTML("beforeend", `
            <div class="reply-form-container" style="margin-top:10px;">
                <form id="reply-post-form-${parentidx}">
                    <input type="hidden" name="boardIdx" value="${BOARD_ID}">
                    <input type="hidden" name="userId" value="${CURRENT_USER_ID}">
                    <input type="hidden" name="parentidx" value="${parentidx}">

                    <textarea name="ment" class="reply-textarea" required>@${parentUserId} </textarea>

                    <div class="reply-actions-group" style="margin-top:5px;">
                        <button type="submit" class="btn-submit-reply">등록</button>
                        <button type="button" class="btn-cancel-reply" onclick="showReplyForm(null)">취소</button>
                    </div>
                </form>
            </div>
        `);

        document.querySelector(`#reply-post-form-${parentidx}`)
            .addEventListener("submit", handleReplySubmit);
    }

    /* ============================
     * 🔥 답글 등록
     * ============================ */
    function handleReplySubmit(e) {
        e.preventDefault();
        const fd = new FormData(e.target);

        const dto = {
            boardIdx: parseInt(fd.get("boardIdx")),
            userId: fd.get("userId"),
            ment: fd.get("ment"),
            parentidx: parseInt(fd.get("parentidx"))
        };

        fetch(COMMENT_API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        })
            .then(res => {
                if (checkAndHandleAuth(res)) return;
                return handleHtmlResponse(res);
            })
            .then(() => {
                _showReplyForm(null);
                fetchComments();
            });
    }

    /* ============================
     * 🔥 댓글 수정 폼 표시 (대댓글 UI 그대로 사용)
     * ============================ */
    function _showEditForm(idx, oldMent) {

        const target = document.getElementById(`coment-text-${idx}`);
        if (!target) return;

        target.innerHTML = `
            <div class="reply-form-container">
                <textarea id="edit-input-${idx}" class="reply-textarea">${oldMent}</textarea>

                <div class="reply-actions-group" style="margin-top:5px;">
                    <button type="button" onclick="updateComent(${idx})" class="btn-submit-reply">저장</button>
                    <button type="button" onclick="fetchComments()" class="btn-cancel-reply">취소</button>
                </div>
            </div>
        `;
    }

    /* ============================
     * 🔥 댓글 수정 요청
     * ============================ */
    function _updateComent(idx) {

        const newMent = document.getElementById(`edit-input-${idx}`).value.trim();
        if (!newMent) {
            alert("내용을 입력하세요.");
            return;
        }

        const dto = {
            idx: idx,
            userId: CURRENT_USER_ID,
            ment: newMent
        };

        fetch(COMMENT_API_URL, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(dto)
        })
            .then(res => handleHtmlResponse(res))
            .then(() => fetchComments());
    }

    /* ============================
     * 🔥 댓글 삭제
     * ============================ */
    function _deleteComent(idx) {
        if (!confirm("삭제하시겠습니까?")) return;

        fetch(`${COMMENT_API_URL}/${idx}?userId=${CURRENT_USER_ID}`, {
            method: "DELETE"
        })
            .then(res => handleHtmlResponse(res))
            .then(() => fetchComments());
    }

    /* ============================
     * 🔥 댓글 렌더링
     * ============================ */
    function renderComments(comments) {

        if (!comments || comments.length === 0) {
            container.innerHTML = "<p style='padding:15px; color:#777;'>등록된 댓글이 없습니다.</p>";
            return;
        }

        const map = {};
        comments.forEach(c => map[c.idx] = { ...c, children: [] });

        const roots = [];
        comments.forEach(c => {
            if (c.parentidx === null) {
                roots.push(map[c.idx]);
            } else if (map[c.parentidx]) {
                map[c.parentidx].children.push(map[c.idx]);
            }
        });

        container.innerHTML = renderTree(roots, 0);

        /* 🔥 댓글 로드 끝난 후 스크롤 이동 */
        setTimeout(() => {
            scrollToHashComment();
        }, 50);
    }

    function highlightMent(text) {
        if (!text) return "";
        return text.replace(/@([a-zA-Z0-9가-힣_]+)/g, '<span class="mention">@$1</span>');
    }

    /* ============================
     * 🔥 댓글 트리 HTML 생성
     * ============================ */
    function renderTree(list, depth) {
        let html = "";

        list.forEach(c => {

            const isMine = c.userId === CURRENT_USER_ID;
            const indent = depth > 0 ? 30 : 0;
            const date = new Date(c.regdate).toLocaleString("ko-KR");

            html += `
                <div class="comment-item depth-${depth}" id="comment-${c.idx}"
                     style="margin-left:${indent}px; margin-top:15px;">

                    <div class="comment-meta">

                        <div class="comment-user">
                            ${depth > 0 ? `<span class='reply-arrow'>↳</span>` : ""}
                            ${c.userId}
                        </div>

                        <div class="comment-info-group">
                            <span class="comment-date">${date}</span>

                            <div class="comment-actions">

                                <button onclick="showReplyForm(${c.idx}, '${c.userId}')">답글쓰기</button>

                                ${isMine ? `

                                    <button onclick="showEditForm(${c.idx}, \`${(c.ment || "").replace(/`/g, "\\`")}\`)">수정</button>

                                    <button onclick="deleteComent(${c.idx})">삭제</button>
                                ` : ""}

                            </div>
                        </div>
                    </div>

                    <div class="coment-text" id="coment-text-${c.idx}">
                        ${highlightMent(c.ment || "")}
                    </div>
                </div>
            `;

            if (c.children.length > 0) {
                html += renderTree(c.children, depth + 1);
            }
        });

        return html;
    }

    /* ============================
     * 🔥 댓글 목록 불러오기
     * ============================ */
    function fetchComments() {
        container.innerHTML = "댓글 불러오는 중...";

        fetch(`${COMMENT_API_URL}/${BOARD_ID}`)
            .then(res => {
                if (checkAndHandleAuth(res)) return;
                return res.json();
            })
            .then(renderComments);
    }

    /* ============================
     * 🔥 해시 기반 스크롤 이동
     * ============================ */
    function scrollToHashComment() {
        const hash = window.location.hash;
        if (!hash || !hash.startsWith("#comment-")) return;

        const target = document.querySelector(hash);
        if (!target) return;

        target.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    /* 초기 로드 */
    fetchComments();

    /* ============================
     * 🔥 전역 바인딩
     * ============================ */
    window.showReplyForm = _showReplyForm;
    window.deleteComent = _deleteComent;
    window.showEditForm = _showEditForm;
    window.updateComent = _updateComent;

    /* -------------------------------------------------------
       * 👍 좋아요 / 👎 싫어요 기능 (최종)
       * ------------------------------------------------------- */
    const likeBtn = document.getElementById("like-btn");
    const dislikeBtn = document.getElementById("dislike-btn");
    const likeCountEl = document.getElementById("like-count");
    const dislikeCountEl = document.getElementById("dislike-count");

    const USER_ID = document.getElementById("current-user-id-hidden")?.value;

    function toggleLike(type) {

        if (!CURRENT_USER_ID) {
            alert("로그인이 필요한 기능입니다.");
            return;
        }

        fetch(`/api/like/${BOARD_ID}/${CURRENT_USER_ID}/${type}`, {
            method: "POST"
        })
            .then(res => res.json())
            .then(data => {

                likeCountEl.innerText = data.likeCount;
                dislikeCountEl.innerText = data.dislikeCount;

                likeBtn.classList.remove("active-like");
                dislikeBtn.classList.remove("active-dislike");

                if (data.result === "added" && type === "like") {
                    likeBtn.classList.add("active-like");
                }
                else if (data.result === "added" && type === "dislike") {
                    dislikeBtn.classList.add("active-dislike");
                }
            });
    }

    // ⭐ HTML에서 호출하도록 전역 등록
    window.toggleLike = toggleLike;

});
