import {bindable, bindingMode, inject,BindingEngine} from 'aurelia-framework';

const concat = (xs,y) => xs.concat(y)
const flatMap = (f,xs) => xs.map(f).reduce(concat, [])

@inject(BindingEngine)
export class SkillsFilterCustomElement {
  @bindable({ defaultBindingMode: bindingMode.twoWay }) skills = [];
  selectedFilters = [];
  allSkills = [];
  tags = [];
  constructor(bindingEngine) {
    this.subscription = bindingEngine.propertyObserver(this, 'skills')
      .subscribe((newValue, oldValue) => this.init(newValue));
  }
  init(skills) {
    this.allSkills = skills
    this.tags = flatMap(s => s.tags, this.allSkills).filter((value, index, self) => self.indexOf(value) === index)
  }
  attached() {
    if(this.skills) this.init(this.skills);
  }
  toggleTag(tag) {
    let index = this.tags.indexOf(tag)
    if(index === -1) {
      this.tags.push(tag)
    }else {
      this.tags.splice(index, -1)
    }
    this.refresh();
  }
  refresh() {
    this.skills = this.allSkills.filter(s => s.tags.some(t => this.tags.indexOf(t) > -1));
  }
  dispose() {
    this.subscription.dispose();
  }
}
