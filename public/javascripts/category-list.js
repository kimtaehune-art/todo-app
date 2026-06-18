// カテゴリー一覧: 削除確認のカスタムモーダル (<dialog>)
// 紐づく Todo も削除されるため、確認を挟む。非同期通信はせず通常 submit。
document.addEventListener('DOMContentLoaded', function () {
  var dialog = document.getElementById('js-category-delete-dialog');
  if (!dialog) return;

  var confirmBtn = dialog.querySelector('.js-delete-confirm');
  var cancelBtn  = dialog.querySelector('.js-delete-cancel');
  var pendingForm = null;

  document.querySelectorAll('.js-category-delete-form').forEach(function (form) {
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      pendingForm = form;
      dialog.showModal();
    });
  });

  confirmBtn.addEventListener('click', function () {
    dialog.close();
    if (pendingForm) pendingForm.submit();
  });

  cancelBtn.addEventListener('click', function () {
    dialog.close();
    pendingForm = null;
  });
});
